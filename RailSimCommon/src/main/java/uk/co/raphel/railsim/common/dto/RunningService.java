package uk.co.raphel.railsim.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.ServiceEventType;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

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
    private String serviceClass;
    private String engine;
    private ServiceEventType latestStatus;


    private DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

   public static RunningService from(TrainService trainService) {
       return from(trainService, trainService.getStartTime());
   }

    public static RunningService from(TrainService trainService, LocalTime simulatorTime) {
        RunningService ret = new RunningService();
        ret.setBlocked(false);
        ret.setCurrentRouteStop(0);
        ret.setOrigin(trainService.getOrigin());
        ret.setActualStartTime(simulatorTime);
        ret.setDestination(trainService.getDestination());
        ret.setActualArrivalTime(LocalTime.MAX);
        ret.setRouteStops(trainService.getRoutePoints().stream().map(r ->  RunningRouteStop.from(r, trainService.getId()))
                .toList().toArray(new RunningRouteStop[0]));

        ret.setScheduledArrivalTime(trainService.getTerminalTime());
        ret.setServiceId(trainService.getId());
        ret.setStartTime(trainService.getStartTime());
        ret.setTerminated(false);
        ret.setEngine(trainService.getEngine());
        ret.setServiceClass(trainService.getServiceClass());
        ret.setLatestStatus(ServiceEventType.NOTREADY);

        return ret;
    }

    /**
     * Snapshot copy, safe to hand to other threads. Route stops are copied too;
     * Berth entities are shared as they are not modified while running.
     */
    public RunningService copy() {
        RunningService ret = new RunningService();
        ret.setServiceId(ServiceId);
        ret.setRouteStops(routeStops == null ? null :
                Arrays.stream(routeStops).map(RunningRouteStop::copy).toArray(RunningRouteStop[]::new));
        ret.setStartTime(startTime);
        ret.setOrigin(origin);
        ret.setDestination(destination);
        ret.setScheduledArrivalTime(scheduledArrivalTime);
        ret.setActualStartTime(actualStartTime);
        ret.setActualArrivalTime(actualArrivalTime);
        ret.setCurrentRouteStop(currentRouteStop);
        ret.setBlocked(blocked);
        ret.setTerminated(terminated);
        ret.setServiceClass(serviceClass);
        ret.setEngine(engine);
        ret.setLatestStatus(latestStatus);
        return ret;
    }

    public String getForLog() {
        return df.format(startTime) + " from " + origin + ". " +
                routeStops.length + " stops. Current stop " + routeStops[currentRouteStop].getBerth().getBerthName();
    }

        public void moveToNextRouteStop(LocalTime simulatorTime) {
        RunningRouteStop lastStop = routeStops[currentRouteStop];
        lastStop.setActualDepartureTime(simulatorTime);
        currentRouteStop++;
        if(currentRouteStop >= routeStops.length-1) {
            terminated = true;
            latestStatus = ServiceEventType.TERMINATING;
        } else {
            lastStop = routeStops[currentRouteStop];
            lastStop.setScheduledArrivalTime(scheduledArrivalTime);
            lastStop.setActualArrivalTime(simulatorTime);
            latestStatus = ServiceEventType.ARRIVESTOP;
        }

    }

    public String getLastBerth() {
        return routeStops[currentRouteStop].getBerth().getBerthName();
    }

    @Deprecated
    public String getNextBerth() {
        if(terminated) {
            return "FINISHED";
        } else {
            return currentRouteStop == routeStops.length - 1 ? "N/A" : routeStops[currentRouteStop + 1].getBerth().getBerthName();
        }
    }


    @Deprecated
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


    public String getTitle() {
       return df.format(startTime) + " from " + getOrigin();
    }
}
