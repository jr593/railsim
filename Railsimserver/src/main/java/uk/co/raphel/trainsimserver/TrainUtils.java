package uk.co.raphel.trainsimserver;

import uk.co.raphel.railsim.common.dto.RunningRouteStop;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.entity.RouteStop;
import uk.co.raphel.railsim.common.entity.TrainService;

import java.time.LocalTime;
import java.util.stream.Collectors;

public class TrainUtils {


    public static RunningRouteStop routePointToRouteStop(RouteStop st) {
        RunningRouteStop ret = new RunningRouteStop();
        ret.setActualArrivalTime(LocalTime.MIN);
        ret.setActualDepartureTime(LocalTime.MIN);
        ret.setBaseServiceId(st.getService().getId());
        ret.setBerth(st.getBerth());
        ret.setId(st.getId());
        ret.setScheduledArrivalTime(st.getArrivalTime());
        ret.setScheduledDepartureTime(st.getDepartureTime());
        return ret;
    }

    public static RunningService trainServiceToRunningService(TrainService trainService) {
        RunningService ret = new RunningService();
        ret.setRouteStops(trainService.getRoutePoints().stream().map(TrainUtils::routePointToRouteStop).collect(Collectors.toList()));
        ret.setOrigin(trainService.getOrigin());
        ret.setStartTime(trainService.getStartTime());
        ret.setActualStartTime(LocalTime.MIN);
        ret.setDestination(trainService.getDestination());
        ret.setActualArrivalTime(LocalTime.MIN);
        ret.setScheduledArrivalTime(trainService.getTerminalTime());
        ret.setServiceId(trainService.getId());

        return ret;
    }
}
