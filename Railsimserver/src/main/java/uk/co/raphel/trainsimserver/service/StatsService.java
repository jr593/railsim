package uk.co.raphel.trainsimserver.service;


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
    private final DashboardBroadcaster broadcaster;

      public StatsService(DashboardBroadcaster broadcaster) {
          this.broadcaster = broadcaster;
      }

    private DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

      // Example: send a new value every second
      @Scheduled(fixedRate = 1000)
      public void sendTimeUpdate() {
          RailSimMessage<SystemStatus> statusMessage = new RailSimMessage<>();
          statusMessage.setMsgKey(UUID.randomUUID());
          statusMessage.setMessageType(MessageType.STATUS);
          SystemStatus status = new SystemStatus(df.format(java.time.LocalTime.now()));
          statusMessage.setMessageBody(status);
          broadcaster.broadcast(statusMessage);
      }

}