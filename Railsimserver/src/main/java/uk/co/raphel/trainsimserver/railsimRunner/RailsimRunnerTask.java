package uk.co.raphel.trainsimserver.railsimRunner;

import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.RunningRouteStop;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.trainsimserver.service.DashboardBroadcaster;
import uk.co.raphel.trainsimserver.service.TrackManagerService;

import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j(topic = "RailSimRunnerTask")
public class RailsimRunnerTask implements Runnable {

    private final TrainService trainService;
    private final DashboardBroadcaster dashboardBroadcaster;

    private final TrackManagerService trackManagerService;

    private Boolean atDestination;

    private static int loopDelay = 60;

    public RailsimRunnerTask(TrainService trainService, DashboardBroadcaster dashboardBroadcaster, TrackManagerService trackManagerService) {
        this.trainService = trainService;
        this.dashboardBroadcaster = dashboardBroadcaster;
        this.trackManagerService = trackManagerService;
        
    }

    public void run() {
        RailSimMessage<RunningService> msg = new RailSimMessage<>();
        msg.setMsgKey(UUID.randomUUID());
        msg.setMessageType(MessageType.SERVICESTART);

        RunningService tracker = RunningService.from(trainService);
        tracker.setDestination(trainService.getDestination());
        tracker.setActualStartTime(LocalTime.now());
        tracker.setOrigin(trainService.getOrigin());
        tracker.setRouteStops(trainService.getRoutePoints().stream()
                .map(p ->  RunningRouteStop.from(p,trainService.getId())).toList().toArray(new RunningRouteStop[0]));
        tracker.setCurrentRouteStop(0);
        msg.setMessageBody(tracker);
        log.info("Created running service :" + tracker.getForLog());
        dashboardBroadcaster.broadcast(msg);

        atDestination = false;

        while (!atDestination) {
            if (LocalTime.now().isAfter(tracker.getRouteStops()[tracker.getCurrentRouteStop()].getScheduledDepartureTime())) {

                // TIME WE MOVED
                if (!trackManagerService.isFree(tracker.getRouteStops()[tracker.getCurrentRouteStop() + 1])) {

                    // WE ARE BLOCKED
                    if (!tracker.isBlocked()) {
                        sendTrackerMessage(MessageType.BLOCKING, tracker);
                        log.warn("Tracker : " + tracker.getForLog() + " has gone BLOCKED");
                        tracker.setBlocked(true);
                    }
                } else {
                    // Free to progress
                    if (tracker.isBlocked()) {
                        log.info("Tracker : " + tracker.getForLog() + " UNBLOCKED");
                        tracker.setBlocked(false);
                    }

                    LocalTime timeToNext = calculatetimeRequiredToNextPassOrStop(tracker);
                    if (timeToNext.isBefore(LocalTime.now())) {
                        if (tracker.isTerminalBerth()) {
                            atDestination = true;
                            log.info("Tracker : " + tracker.getForLog() + " is FINISHED.");
                            sendTrackerMessage(MessageType.COMPLETION, tracker);
                        } else {
                            tracker.moveToNextRouteStop(timeToNext);
                            log.info("Tracker : " + tracker.getForLog() + " moved to " + tracker.getLastBerth());
                            sendTrackerMessage(MessageType.MOVEMENT, tracker);
                        }
                    }
                }
            } else {
                // Time TO WAIT FOR NEXT SCHEDULED MOVE
                log.info("Service " + tracker.getStartTime() + " from " + tracker.getOrigin() + " loop wait.");
            }
            try {
                Thread.sleep(Duration.ofSeconds(loopDelay));
            } catch (InterruptedException e) {
                log.error("Tracker " + tracker.getOrigin() + " (" + tracker.getStartTime() + ") thread interrupt", e);
            }
        }
    }

    private LocalTime calculatetimeRequiredToNextPassOrStop(RunningService tracker) {
        // Find the time that the service should be at the next routepoint.
        RunningRouteStop stop = tracker.getRouteStops()[tracker.getCurrentRouteStop()];
        double lengthOfBerth = stop.getBerth().getLengthMiles();
        int speedLimit = stop.getBerth().getSpeedLimit();
        double timeToTransit = 60 * lengthOfBerth / (double) (speedLimit);
        return stop.getActualArrivalTime().plusMinutes((int) timeToTransit);

    }

    private void sendTrackerMessage(MessageType type, RunningService tracker) {
        RailSimMessage<RunningService> msg = new RailSimMessage<>();
        msg.setMsgKey(UUID.randomUUID());
        msg.setMessageType(type);
        msg.setMessageBody(tracker);
        log.info("Runner broadcast Service = " + tracker.getStartTime() + " from " + tracker.getOrigin() +
                " Msg type " + type + " Current berth " + tracker.getRouteStops()[tracker.getCurrentRouteStop()]);
        dashboardBroadcaster.broadcast(msg);
    }


}
