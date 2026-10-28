package uk.co.raphel.trainsimserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.http.TrainsResponse;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j(topic="RestService")
public class RestService {


    DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

    private final TimerService timerService;

    public RestService(TimerService timerService) {
        this.timerService = timerService;
           }

    @GetMapping("/api/getTrain/{startTime}/{from}/")
    public TrainsResponse getTrains(@PathVariable("startTime") final String starTime,
                                    @PathVariable("from") final String origin) {
        TrainsResponse resp = new TrainsResponse() ;
        try {
            df.parse(starTime);
        } catch (Exception e) {
            log.error("Invalid start time");
            resp.setErrorMessage("");
            resp.setSuccess(false);
        }
        resp.setSuccess(true);
        if(origin == null || origin.isEmpty()) {
            log.error("Invalid origin");
            resp.setErrorMessage("");
            resp.setSuccess(false);
        }
        if(!resp.getSuccess()) {
            resp.setErrorMessage("Invalid time format, or origin not found");
            resp.setTrains(new ArrayList<>());
            return resp;
        }
        List<RunningService> servs = timerService.getTimeTableEntries(starTime, origin);
        resp.setTrains(servs);
        resp.setErrorMessage("");
        resp.setSuccess(true);
        return resp;
    }
}
