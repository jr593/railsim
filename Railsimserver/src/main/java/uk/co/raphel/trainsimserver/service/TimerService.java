package uk.co.raphel.trainsimserver.service;

import com.vaadin.flow.component.UI;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cglib.core.Local;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.dto.SystemStatus;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;

import uk.co.raphel.trainsimserver.exception.InvalidTimeTableEntryException;
import uk.co.raphel.trainsimserver.railsimRunner.RailsimRunnerTask;
import uk.co.raphel.trainsimserver.repository.TrainServiceRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@Slf4j(topic = "TimerService")
public class TimerService implements BroadcastListener {

    private final TaskExecutor taskExecutor;
    private final TrainServiceRepository trainServiceRepository;
    List<TrainService> timeTable;
    private final TrackManagerService trackManagerService;
    private final MessageBroadcaster messageBroadcaster;
    private final ProfileService profileService;
    private final TrainManagerService trainMonitor;

    private LocalTime simulatorTime = LocalTime.MIN;

    private List<Long> startedServices = new ArrayList<>();

    public TimerService(
            @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
            TrainServiceRepository trainServiceRepository, TrackManagerService trackManagerService,
            MessageBroadcaster messageBroadcaster, ProfileService profileService, TrainManagerService trainMonitor) {
        this.taskExecutor = taskExecutor;
        this.trainServiceRepository = trainServiceRepository;
        this.trackManagerService = trackManagerService;
        this.messageBroadcaster = messageBroadcaster;
        this.profileService = profileService;
        this.trainMonitor = trainMonitor;
    }

    @PostConstruct
    public void init() {
        messageBroadcaster.register(this);
        timeTable = loadTimeTable();
        // Pre mark timetable entries that should be skipped
        timeTable.stream().filter(t -> t.getStartTime().isBefore(LocalTime.now()))
                .forEach(t -> t.setStarted(true));
    }

    @Scheduled(fixedRate = 5000, initialDelay = 10000)
    public void timer() {

//          log.info("Checking for starters");
        LocalTime currentTriggerTime = simulatorTime.truncatedTo(ChronoUnit.MINUTES);


//        log.info("Next departures");
        List<TrainService> upcomingServices = getNextDepartures(currentTriggerTime, 3);
//        for(TrainService t : upcomingServices ) {
//            log.info("               " + t.toString());
//        }
        // TODO Trains must set off at time of dirst routestop, not time from origin.
        List<TrainService> toDepart = timeTable.stream()
                .filter(t -> isDueOff(t, currentTriggerTime) && !startedServices.contains(t.getId()))
                .toList();

        for (TrainService s : toDepart) {
            startService(s, trainMonitor, currentTriggerTime, messageBroadcaster);
            startedServices.add(s.getId());
            // Task runner responsible for logging running service actions
        }
        // Notify upcoming services
        RailSimMessage<List<RunningService>> msg = new RailSimMessage<>();
        msg.setMsgKey(UUID.randomUUID());
        msg.setMessageType(MessageType.SCHEDULE);
        List<RunningService> msgServices = new ArrayList<>();
        for (TrainService trainService : upcomingServices) {
            msgServices.add(RunningService.from(trainService, simulatorTime));
        }
        msg.setMessageBody(msgServices);

        messageBroadcaster.broadcast(msg);

    }

    private boolean isDueOff(TrainService trainService, LocalTime currentTriggerTime) {
        LocalTime servTime = trainService.getStartTime();
        return !trainService.isStarted() && (currentTriggerTime.withSecond(0).isAfter(servTime) ||
                currentTriggerTime.withSecond(0).equals(servTime));

    }

    private void startService(TrainService trainService, TrainManagerService trainMonitor, LocalTime triggerTime, MessageBroadcaster messageBroadcaster) {
        trainService.setStarted(true);
        taskExecutor.execute(new RailsimRunnerTask(trainService, messageBroadcaster, trackManagerService, trainMonitor));
        // needRefresh = true;
        log.info(trainService.getStartTime() + " " + trainService.getOrigin() + " to " + trainService.getDestination() + " sets off");

    }

    @Override
    public void onMessage(RailSimMessage<?> message) {
        if (message.getMessageType() == MessageType.STATUS) {
            LocalTime newTime = ((SystemStatus) message.getMessageBody()).simulatorTime();
            if (newTime.isBefore(simulatorTime)) {
                timeTable.forEach(t -> t.setStarted(false));

            }
            simulatorTime = newTime;
        }
    }

    @Override
    public Optional<UI> retrieveUI() {
        return Optional.empty();
    }

    public List<TrainService> loadTimeTable() {
        if (profileService.isProfileActive("test")) {
            Optional<TrainService> t =
                    trainServiceRepository.findFirstByStartTimeAfterOrderByStartTimeAsc(LocalTime.now());
            if (t.isPresent()) {
                return new ArrayList<>(List.of(t.get()));
            }
            return new ArrayList<>();
        } else {
            List<TrainService> ret = trainServiceRepository.findAll();
            ret.stream().forEach(r -> r.setStarted(false));
            return ret;
        }
    }


    private List<TrainService> getNextDepartures(LocalTime triggerTime, int numberOfServices) {
        List<TrainService> ret = new ArrayList<>();
        for (TrainService t : timeTable) {
            if (t.getStartTime().isAfter(triggerTime)) {
                ret.add(t);
            }
            if (ret.size() == numberOfServices) {
                return ret; // Only the first three
            }
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

