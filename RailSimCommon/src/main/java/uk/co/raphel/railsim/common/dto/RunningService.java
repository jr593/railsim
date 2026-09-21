package uk.co.raphel.railsim.common.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;
import uk.co.raphel.railsim.common.entity.TrainService;

import java.time.LocalTime;
import java.util.List;

/*
 Used to hold the details of aservice while it is running
 */
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class RunningService {

    private Long ServiceId;
    private List<RunningRouteStop> routeStops;
    private LocalTime startTime;
    private String origin;
    private String destination;
    private LocalTime scheduledArrivalTime;
    private LocalTime actualStartTime;
    private LocalTime actualArrivalTime;



}
