package uk.co.raphel.railsim.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.raphel.railsim.common.entity.Berth;

import java.time.LocalTime;

/*
  Holds data relevant to routestops of a running service
 */
@NoArgsConstructor @AllArgsConstructor
@Getter
@Setter
public class RunningRouteStop {

    private long id;
    private Berth berth;
    private Long baseServiceId;
    private LocalTime scheduledArrivalTime;
    private LocalTime scheduledDepartureTime;
    private LocalTime actualArrivalTime;
    private LocalTime actualDepartureTime;
}

