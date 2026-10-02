package uk.co.raphel.trainsimserver.service;

import com.vaadin.flow.component.UI;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cglib.core.Local;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.dto.SystemStatus;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;

import uk.co.raphel.trainsimserver.railsimRunner.RailsimRunnerTask;
import uk.co.raphel.trainsimserver.repository.TrainServiceRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j(topic = "TimerService")
public class TimerService implements BroadcastListener {

    private final TaskExecutor taskExecutor;
    private final TrainServiceRepository trainServiceRepository;
    List<TrainService> timeTable;
    private final TrackManagerService trackManagerService;
    private final MessageBroadcaster messageBroadcaster;

    private LocalTime simulatorTime = LocalTime.MIN;

    private List<Long> startedServices = new ArrayList<>();

    public TimerService(
            @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
            TrainServiceRepository trainServiceRepository, TrackManagerService trackManagerService,
            MessageBroadcaster messageBroadcaster) {
        this.taskExecutor = taskExecutor;
        this.trainServiceRepository = trainServiceRepository;
        this.trackManagerService = trackManagerService;
        this.messageBroadcaster = messageBroadcaster;
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

          log.info("Checking for starters");
         LocalTime currentTriggerTime = simulatorTime.truncatedTo(ChronoUnit.MINUTES);


        log.info("Next departures");
        List<TrainService> upcomingServices = getNextDepartures(currentTriggerTime);
        for(TrainService t : upcomingServices ) {
            log.info("               " + t.toString());
        }
        List<TrainService> toDepart = timeTable.stream()
                .filter(t -> isDueOff(t, currentTriggerTime) && !startedServices.contains(t.getId()))
                .toList();

        for(TrainService s : toDepart) {
            startService(s, currentTriggerTime, messageBroadcaster);
            startedServices.add(s.getId());
            // Task runner responsible for logging running service actions
        }
        // Notify upcoming services
        RailSimMessage<List<RunningService>> msg =  new RailSimMessage<>();
        msg.setMsgKey(UUID.randomUUID());
        msg.setMessageType(MessageType.SCHEDULE);
        List<RunningService> msgServices = new ArrayList<>();
        for(TrainService trainService : upcomingServices) {
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

    private void startService(TrainService trainService, LocalTime triggerTime, MessageBroadcaster messageBroadcaster) {
        trainService.setStarted(true);
        taskExecutor.execute(new RailsimRunnerTask(trainService, messageBroadcaster, trackManagerService));
       // needRefresh = true;
        log.info(trainService.getStartTime() + " " + trainService.getOrigin() + " to " + trainService.getDestination() + " sets off");

    }

    @Override
    public void onMessage(RailSimMessage<?> message) {
        if(message.getMessageType() == MessageType.STATUS) {
            LocalTime newTime = ((SystemStatus)message.getMessageBody()).simulatorTime();
            if(newTime.isBefore(simulatorTime)) {
                timeTable.forEach(t -> t.setStarted(false));

            }
            simulatorTime = newTime;
        }
    }

    @Override
    public Optional<UI> retrieveUI() {
        return Optional.empty();
    }

    private List<TrainService> loadTimeTable() {
        List<TrainService> ret = trainServiceRepository.findAll();
        ret.stream().forEach(r -> r.setStarted(false));
        return ret;
    }

    private List<TrainService> getNextDepartures(LocalTime triggerTime) {
        List<TrainService> ret = new ArrayList<>();
        for(TrainService t : timeTable) {
            if(t.getStartTime().isAfter(triggerTime)) {
                ret.add(t);
            }
            if(ret.size() == 3) {
                return ret; // Only the first three
            }
        }
        return ret;
    }
}
