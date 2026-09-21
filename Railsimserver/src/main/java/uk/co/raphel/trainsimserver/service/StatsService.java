package uk.co.raphel.trainsimserver.service;


import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.SystemStatus;
import uk.co.raphel.railsim.common.enums.MessageType;

import java.util.UUID;


@Service
@EnableScheduling
public class StatsService {
    private final DashboardBroadcaster broadcaster;

      public StatsService(DashboardBroadcaster broadcaster) {
          this.broadcaster = broadcaster;
      }

      // Example: send a new value every second
      @Scheduled(fixedRate = 1000)
      public void sendTimeUpdate() {
          RailSimMessage<SystemStatus> statusMessage = new RailSimMessage<>();
          statusMessage.setMsgKey(UUID.randomUUID());
          statusMessage.setMessageType(MessageType.STATUS);
          SystemStatus status = new SystemStatus(java.time.LocalTime.now().toString());

          broadcaster.broadcast(statusMessage);
      }

}