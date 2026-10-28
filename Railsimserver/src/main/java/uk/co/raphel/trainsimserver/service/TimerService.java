package uk.co.raphel.trainsimserver.service;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import uk.co.raphel.broadcast.api.BroadcastBus;
import uk.co.raphel.broadcast.api.MessageHandler;
import uk.co.raphel.broadcast.api.Subscription;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.entity.TrainService;

import uk.co.raphel.railsim.common.nats.StatsMessage;
import uk.co.raphel.railsim.common.nats.UpcomingDeparturesMessage;
import uk.co.raphel.trainsimserver.railsimRunner.RailsimRunnerTask;
import uk.co.raphel.trainsimserver.repository.TrainServiceRepository;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@Slf4j(topic = "TimerService")
public class TimerService  {

    private static final int NUMBER_UPCOMING_SERVICES = 10;
    // The upcoming list is re-sent every N clock ticks even when unchanged,
    // so newly opened dashboards receive it promptly.
    private static final int SCHEDULE_REBROADCAST_TICKS = 5;

    private final TaskExecutor taskExecutor;
    private final TrainServiceRepository trainServiceRepository;
    // Sorted by start time (see TrainServiceRepository.findAllWithRoutePoints); not modified after init.
    List<TrainService> timeTable;
    private final TrackManagerService trackManagerService;
    private final MessageBroadcaster messageBroadcaster;
    private final ProfileService profileService;
    private final TrainManagerService trainMonitor;

    // null until the first STATUS message arrives
    private LocalTime simulatorTime;

    // Index into timeTable of the next service due to depart today.
    private int nextDepartureIdx = 0;

    // Cached SCHEDULE body; only rebuilt when nextDepartureIdx moves.
    private List<RunningService> upcomingServices = List.of();
    private int upcomingServicesIdx = -1;
    private int ticksSinceScheduleSent = 0;

    private final BroadcastBus broadcastBus;

    private final List<Subscription> subscriptions = new ArrayList<>();


    public TimerService(
            @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
            TrainServiceRepository trainServiceRepository, TrackManagerService trackManagerService,
            MessageBroadcaster messageBroadcaster, ProfileService profileService, TrainManagerService trainMonitor, BroadcastBus broadcastBus) {
        this.taskExecutor = taskExecutor;
        this.trainServiceRepository = trainServiceRepository;
        this.trackManagerService = trackManagerService;
        this.messageBroadcaster = messageBroadcaster;
        this.profileService = profileService;
        this.trainMonitor = trainMonitor;
        this.broadcastBus = broadcastBus;
    }


    public void publish(Object message) {

    }

    public <T> void subscribe(Class<T> messageClass, MessageHandler<T> handler) {

        Subscription subscription = broadcastBus.subscribe(
                messageClass,
                handler
        );

        subscriptions.add(subscription);
    }

    @PreDestroy
    public void unsubscribe() {
        subscriptions.forEach(Subscription::close);
        subscriptions.clear();
    }
    @PostConstruct
    public void init() {

        this.subscribe(StatsMessage.class, this::handleStatus);
        timeTable = loadTimeTable();
    }

    private void handleStatus(Object msg) {
        simulatorTime = ((StatsMessage) msg).getMessageBody();
        onClockTick(simulatorTime);
    }


    /*
     * Departures are driven by the StatsService clock tick rather than a separate schedule,
     * so services set off in step with simulator time.
     * synchronized: successive ticks may arrive on different (virtual) scheduler threads.
     */
    private synchronized void onClockTick(LocalTime newTime) {
        if (simulatorTime == null) {
            // First tick - skip services that should already have departed.
            nextDepartureIdx = firstIndexNotBefore(newTime);
        } else if (newTime.isBefore(simulatorTime)) {
            // Midnight rollover - start any of yesterday's stragglers, then begin the new day.
            startDueServices(LocalTime.MAX);
            nextDepartureIdx = 0;
        }
        simulatorTime = newTime;

        // TODO Trains must set off at time of first routestop, not time from origin.
        startDueServices(newTime.truncatedTo(ChronoUnit.MINUTES));
        broadcastUpcomingServices();
    }

    private void startDueServices(LocalTime triggerTime) {
        while (nextDepartureIdx < timeTable.size()
                && !timeTable.get(nextDepartureIdx).getStartTime().isAfter(triggerTime)) {
            startService(timeTable.get(nextDepartureIdx++));
        }
    }

    private void startService(TrainService trainService) {
        taskExecutor.execute(new RailsimRunnerTask(trainService, messageBroadcaster, trackManagerService, trainMonitor));
        log.info(trainService.getStartTime() + " " + trainService.getOrigin() + " to " + trainService.getDestination() + " sets off");
    }

    private void broadcastUpcomingServices() {
        boolean changed = upcomingServicesIdx != nextDepartureIdx;
        if (changed) {
            upcomingServicesIdx = nextDepartureIdx;
            upcomingServices = getNextDepartures(NUMBER_UPCOMING_SERVICES).stream()
                    .map(t -> RunningService.from(t, simulatorTime))
                    .toList();
        }
        if (!changed && ++ticksSinceScheduleSent < SCHEDULE_REBROADCAST_TICKS) {
            return;
        }
        ticksSinceScheduleSent = 0;

        UpcomingDeparturesMessage msg = new UpcomingDeparturesMessage(UUID.randomUUID(), upcomingServices);
        messageBroadcaster.broadcast(msg);
    }

    public List<TrainService> loadTimeTable() {
        if (profileService.isProfileActive("test")) {
            Optional<TrainService> t =
                    trainServiceRepository.findFirstByStartTimeAfterOrderByStartTimeAsc(LocalTime.now());
            return t.<List<TrainService>>map(trainService -> new ArrayList<>(List.of(trainService))).orElseGet(ArrayList::new);
        } else {
            return trainServiceRepository.findAllWithRoutePoints();
        }
    }

    private int firstIndexNotBefore(LocalTime time) {
        int i = 0;
        while (i < timeTable.size() && timeTable.get(i).getStartTime().isBefore(time)) {
            i++;
        }
        return i;
    }

    private List<TrainService> getNextDepartures(int numberOfServices) {
        List<TrainService> ret = new ArrayList<>(numberOfServices);
        // Wraps past midnight so late-evening dashboards show the next morning's services.
        for (int i = 0; i < timeTable.size() && ret.size() < numberOfServices; i++) {
            ret.add(timeTable.get((nextDepartureIdx + i) % timeTable.size()));
        }
        return ret;
    }

    public List<RunningService> getTimeTableEntries(String startTime, String origin) {
        // Get one service at present, but setup for future expansion
        List<RunningService> ret = new ArrayList<>();
        int hour = Integer.parseInt(startTime.substring(0, 2));
        int minute = Integer.parseInt(startTime.substring(3, 5));
        for (TrainService service : timeTable) {
            RunningService runner = RunningService.from(service);
            if (service.getOrigin().equalsIgnoreCase(origin) && service.getStartTime().getHour() == hour &&
                    service.getStartTime().getMinute() == minute) {
                log.info("service " + runner.getForLog() + " matches.!!");
                ret.add(runner);

            } else {
                log.info("Service " + runner.getForLog() + " DOES NOT MATCH");
            }
        }
        return ret;
    }
}

