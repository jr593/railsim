package uk.co.raphel.railsim.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;
import uk.co.raphel.railsim.common.entity.Berth;
import uk.co.raphel.railsim.common.entity.RouteStop;

import java.time.LocalTime;
import java.util.UUID;

/*
  Holds data relevant to routestops of a running service
 */
@AllArgsConstructor
@Getter
@Setter
public class RunningRouteStop {

    private UUID id;
    private Berth berth;
    private Long baseServiceId;
    private LocalTime scheduledArrivalTime;
    private LocalTime scheduledDepartureTime;
    private LocalTime actualArrivalTime;
    private LocalTime actualDepartureTime;

    public RunningRouteStop() {
        setId(UUID.randomUUID());
    }

    public static RunningRouteStop from(RouteStop routeStop, long baseServiceId) {
        RunningRouteStop ret = new RunningRouteStop();
        ret.setActualArrivalTime(LocalTime.MAX);
        ret.setScheduledArrivalTime(routeStop.getArrivalTime());
        ret.setActualDepartureTime(LocalTime.MAX);
        ret.setScheduledDepartureTime(routeStop.getDepartureTime());
        ret.setId(UUID.randomUUID());
        ret.setBerth(routeStop.getBerth());
        ret.setBaseServiceId(baseServiceId);

        return ret;
    }
}

