package uk.co.raphel.trainsimserver.service;


import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class StatsService {
    private final DashboardBroadcaster broadcaster;

      public StatsService(DashboardBroadcaster broadcaster) {
          this.broadcaster = broadcaster;
      }

      // Example: send a new value every second
      @Scheduled(fixedRate = 1000)
      public void sendOrderUpdate() {
          String newValue = java.time.LocalTime.now().toString();
          broadcaster.broadcast(newValue);
      }

}