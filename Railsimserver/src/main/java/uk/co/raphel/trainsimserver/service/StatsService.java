package uk.co.raphel.trainsimserver.service;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.SystemStatus;
import uk.co.raphel.railsim.common.enums.MessageType;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Service
@EnableScheduling
public class StatsService {
    private final MessageBroadcaster broadcaster;

    private LocalTime simTime = LocalTime.now();

    @Value("${sim.rate-multiplier:1}")
    private int clockRateMultiplier;


      public StatsService(MessageBroadcaster broadcaster) {
          this.broadcaster = broadcaster;

      }

    @PostConstruct
    public void init() {
        simTime = LocalTime.now();
    }


    private DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

      // Example: send a new value every second
      @Scheduled(fixedRate = 1000, initialDelay = 5000)
      public void sendTimeUpdate() {
          RailSimMessage<SystemStatus> statusMessage = new RailSimMessage<>();
          statusMessage.setMsgKey(UUID.randomUUID());
          statusMessage.setMessageType(MessageType.STATUS);
          SystemStatus status = new SystemStatus(simTime);
          simTime = simTime.plusSeconds(clockRateMultiplier);
          statusMessage.setMessageBody(status);
          broadcaster.broadcast(statusMessage);
      }

}