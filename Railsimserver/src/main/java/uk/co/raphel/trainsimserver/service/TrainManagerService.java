package uk.co.raphel.trainsimserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.enums.ServiceEventType;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j(topic="TrainManagerService")
public class TrainManagerService {

    // Keyed by serviceId; updated concurrently by every RailsimRunnerTask thread.
    private final Map<Long, RunningService> services = new ConcurrentHashMap<>();

    DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");


    public void accept(RunningService runningService) {
        accept(runningService, runningService.getLatestStatus());
    }
    // Log train action
    public void accept(RunningService service, ServiceEventType evtType) {
        services.compute(service.getServiceId(), (id, existing) -> {
            if (existing == null) {
                return service;
            }
            existing.setLatestStatus(evtType);
            return existing;
        });
    }

}
