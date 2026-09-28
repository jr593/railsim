package uk.co.raphel.railsim.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.raphel.railsim.common.entity.TrainService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/*
 Used to hold the details of aservice while it is running
 */
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class RunningService {

    private Long ServiceId;
    private RunningRouteStop[] routeStops;
    private LocalTime startTime;
    private String origin;
    private String destination;
    private LocalTime scheduledArrivalTime;
    private LocalTime actualStartTime;
    private LocalTime actualArrivalTime;
    private int currentRouteStop;
    private boolean blocked;
    private boolean terminated;

    private DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

    public static RunningService from(TrainService trainService) {
        RunningService ret = new RunningService();
        ret.setBlocked(false);
        ret.setCurrentRouteStop(0);
        ret.setOrigin(trainService.getOrigin());
        ret.setActualStartTime(LocalTime.now());
        ret.setDestination(trainService.getDestination());
        ret.setActualArrivalTime(LocalTime.MAX);
        ret.setRouteStops(trainService.getRoutePoints().stream().map(r ->  RunningRouteStop.from(r, trainService.getId()))
                .toList().toArray(new RunningRouteStop[0]));

        ret.setScheduledArrivalTime(trainService.getTerminalTime());
        ret.setServiceId(trainService.getId());
        ret.setStartTime(trainService.getStartTime());
        ret.setTerminated(false);

        return ret;
    }

    public String getForLog() {
        return df.format(startTime) + " from " + origin + ". " +
                routeStops.length + " stops. Current stop" + routeStops[currentRouteStop].getBerth().getBerthName();
    }

        public void moveToNextRouteStop(LocalTime scheduledTimeForNext) {
        RunningRouteStop lastStop = routeStops[currentRouteStop];
        lastStop.setActualDepartureTime(LocalTime.now());
        currentRouteStop++;
        if(currentRouteStop >= routeStops.length) {
            terminated = true;
        } else {
            lastStop = routeStops[currentRouteStop];
            lastStop.setScheduledArrivalTime(scheduledArrivalTime);
            lastStop.setActualArrivalTime(LocalTime.now());
        }

    }

    public String getLastBerth() {
        return routeStops[currentRouteStop].getBerth().getBerthName();
    }

    public String getNextBerth() {
        if(terminated) {
            return "FINISHED";
        } else {
            return routeStops[currentRouteStop+1].getBerth().getBerthName();
        }
    }


    public String getNextBerthDueAt() {
        if(terminated) {
            return "N/A";
        } else {
            return df.format(routeStops[currentRouteStop+1].getScheduledArrivalTime());
        }
    }

    public boolean isTerminalBerth() {
        return currentRouteStop == routeStops.length-1;
    }


}
