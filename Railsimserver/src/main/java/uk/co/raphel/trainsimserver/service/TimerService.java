package uk.co.raphel.trainsimserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.trainsimserver.railsimRunner.RailsimRunnerTask;
import uk.co.raphel.trainsimserver.repository.TrainServiceRepository;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j(topic = "TimerService")
public class TimerService {

    private final TaskExecutor taskExecutor;
    private final TrainServiceRepository trainServiceRepository;
    List<TrainService> servicesDue;

    private final DashboardBroadcaster dashboardBroadcaster;

    public TimerService(
            @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
            TrainServiceRepository trainServiceRepository, DashboardBroadcaster dashboardBroadcaster) {
        this.taskExecutor = taskExecutor;
        this.trainServiceRepository = trainServiceRepository;
        this.dashboardBroadcaster = dashboardBroadcaster;
    }

    boolean needRefresh = false;

    @Scheduled(fixedRate = 60000)
    public void timer() {

        log.info("Checking for starters");
         LocalTime currentTriggerTime = LocalTime .now().truncatedTo(ChronoUnit.MINUTES);

         servicesDue = getNextDepartures(currentTriggerTime);

        log.info("Next departures = {}", servicesDue);
        if (servicesDue != null && !servicesDue.isEmpty()) {
            servicesDue.stream().filter(s -> s.getStartTime().equals(currentTriggerTime))
                    .forEach(s -> startService(s,currentTriggerTime , dashboardBroadcaster));


            if(needRefresh) {
                servicesDue = getNextDepartures(currentTriggerTime);
            }
        }
        RailSimMessage<List<TrainService>> msg =  new RailSimMessage<>();
        msg.setMsgKey(UUID.randomUUID());
        msg.setMessageType(MessageType.SCHEDULE);
        msg.setMessageBody(servicesDue);

        dashboardBroadcaster.broadcast(msg);
    }

    private void startService(TrainService trainService, LocalTime triggerTime, DashboardBroadcaster dashboardBroadcaster) {
        taskExecutor.execute(new RailsimRunnerTask(trainService, dashboardBroadcaster));
        needRefresh = true;

    }
    private List<TrainService> getNextDepartures(LocalTime triggerTime) {
        return trainServiceRepository.findNextDepartures(
                triggerTime,
                PageRequest.of(0, 3)
        );
    }
}
