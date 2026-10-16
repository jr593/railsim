package uk.co.raphel.trainsimserver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.enums.ServiceEventType;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j(topic="TrainManagerService")
public class TrainManagerService {

   private List<RunningService> services;



    DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");


    @PostConstruct
    public void init() {

        services = new ArrayList<>();
    }


    public void accept(RunningService runningService) {
        accept(runningService, runningService.getLatestStatus());
    }
    // Log train action
    public void accept(RunningService service, ServiceEventType evtType) {
       Optional<RunningService> toChange = services.stream().filter(r -> Objects.equals(r.getServiceId(), service.getServiceId()))
                .findFirst();
       if(toChange.isPresent()) {

           toChange.get().setLatestStatus(evtType);
       } else {
           services.add(service);

       }
    }

}
