package uk.co.raphel.trainsimserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.trainsimserver.railsimRunner.RailsimRunnerTask;
import uk.co.raphel.trainsimserver.repository.TrainServiceRepository;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;



@Service
@Slf4j(topic = "TimerService")
public class TimerService {

    private final TaskExecutor taskExecutor;
    private final TrainServiceRepository trainServiceRepository;

    public TimerService(
            @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
            TrainServiceRepository trainServiceRepository) {
        this.taskExecutor = taskExecutor;
        this.trainServiceRepository = trainServiceRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void timer() {

        log.info("Checking for starters");
         LocalTime currentTriggerTime = LocalTime .now().truncatedTo(ChronoUnit.MINUTES);

        //LocalTime.now().withSecond(0);
        List<TrainService> trainServices =
                trainServiceRepository.findNextDepartures(
                        currentTriggerTime,
                        PageRequest.of(0, 3)
                );

        log.info("Next departures = {}", trainServices);
        if (trainServices != null && !trainServices.isEmpty()) {
            trainServices.forEach(trainService -> {
                        if (trainService.getStartTime().equals(currentTriggerTime)) {
                            taskExecutor.execute(new RailsimRunnerTask(trainService));
                        } else {
                            log.info("Not ready yet for service {}", trainService);
                        }
                    }
            );
        }
    }
}
