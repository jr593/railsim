package uk.co.raphel.trainsimserver.service;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.nats.StatsMessage;


import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Service
public class StatsService {
    private final MessageBroadcaster broadcaster;

    // Sim time is derived from elapsed real time since these were captured,
    // so late or skipped ticks don't make the clock drift.
    private long startNanos;
    private LocalTime startTime;

    @Value("${sim.rate-multiplier:1}")
    private int clockRateMultiplier;


      public StatsService(MessageBroadcaster broadcaster) {
          this.broadcaster = broadcaster;

      }

    @PostConstruct
    public void init() {
        startNanos = System.nanoTime();
        startTime = LocalTime.now();
    }


      // Send the simulator time every second
      // synchronized: with virtual threads the scheduler may overlap fixed-rate runs
      @Scheduled(fixedRate = 1000, initialDelay = 5000)
      public synchronized void sendTimeUpdate() {
          LocalTime simTime = startTime
                  .plusNanos((System.nanoTime() - startNanos) * clockRateMultiplier)
                  .truncatedTo(ChronoUnit.SECONDS);
          broadcaster.broadcast(new StatsMessage(UUID.randomUUID(), simTime));
      }

}
