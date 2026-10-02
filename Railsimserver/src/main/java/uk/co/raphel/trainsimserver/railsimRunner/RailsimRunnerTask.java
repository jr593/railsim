package uk.co.raphel.trainsimserver.railsimRunner;

import com.vaadin.flow.component.UI;
import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.RunningRouteStop;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.dto.SystemStatus;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.trainsimserver.service.BroadcastListener;
import uk.co.raphel.trainsimserver.service.MessageBroadcaster;
import uk.co.raphel.trainsimserver.service.TrackManagerService;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Slf4j(topic = "RailSimRunnerTask")
public class RailsimRunnerTask implements Runnable, BroadcastListener {

    private final RunningService runningService;
    private final MessageBroadcaster messageBroadcaster;

    private LocalTime simulatorTime;

    private final TrackManagerService trackManagerService;

    private Boolean atDestination;

    private static int loopDelay = 60;

    private boolean receivingMessages ;

    private DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

    public RailsimRunnerTask(TrainService trainService, MessageBroadcaster messageBroadcaster, TrackManagerService trackManagerService) {
        this.runningService = RunningService.from(trainService);
        this.messageBroadcaster = messageBroadcaster;
        this.trackManagerService = trackManagerService;
        receivingMessages = false;
    }

    // Process one Train Service
    public void run() {
        messageBroadcaster.register(this);
        RailSimMessage<RunningService> msg = new RailSimMessage<>();
        msg.setMsgKey(UUID.randomUUID());
        msg.setMessageType(MessageType.SERVICESTART);

       //runningService.setDestination(runningService.getDestination());
        runningService.setActualStartTime(simulatorTime);
      //  runningService.setOrigin(runningService.getOrigin());
      //  runningService.setRouteStops(runningService.getRoutePoints().stream()
       //         .map(p ->  RunningRouteStop.from(p, runningService.getId())).toList().toArray(new RunningRouteStop[0]));
        runningService.setCurrentRouteStop(0);
        // we 'arrive' at the origin as we start.
        runningService.getRouteStops()[runningService.getCurrentRouteStop()].setActualArrivalTime(runningService.getActualStartTime());
        msg.setMessageBody(runningService);
        log.info("Created running service :" + runningService.getForLog());
        messageBroadcaster.broadcast(msg);

        atDestination = false;

        while (!atDestination && receivingMessages) {
            if (simulatorTime.isAfter(runningService.getRouteStops()[runningService.getCurrentRouteStop()].getScheduledDepartureTime())) {

                // TIME WE MOVED
                if (!trackManagerService.isFree(runningService.getRouteStops()[runningService.getCurrentRouteStop() + 1])) {
                    log.info("running service  " + runningService.getForLog() + " blocked next section..");
                    // WE ARE BLOCKED
                    if (!runningService.isBlocked()) {
                        sendRunningServiceMessage(MessageType.BLOCKING, runningService);
                        log.warn("runningService : " + runningService.getForLog() + " has gone BLOCKED");
                        runningService.setBlocked(true);
                    } else {
                        log.warn("running service " + runningService.getForLog() + " already blocked");
                    }
                } else {
                    // Free to progress
                    log.info("running service " + runningService.getForLog() + " free to move");

                    if (runningService.isBlocked()) {
                        log.info("runningService : " + runningService.getForLog() + " UNBLOCKED");
                        runningService.setBlocked(false);
                    }

                    LocalTime timeToNext = calculatetimeRequiredToNextPassOrStop(runningService);
                    log.info("time check for " + runningService.getForLog() + " simTime=" + df.format(simulatorTime)
                                                            + " next= " + df.format(timeToNext));
                    if (timeToNext.isBefore(simulatorTime)) {
                        log.info("Time expired " + runningService.getForLog() + " move me");
                        if (runningService.isTerminalBerth()) {
                            atDestination = true;
                            log.info("runningService : " + runningService.getForLog() + " is FINISHED.");
                            sendRunningServiceMessage(MessageType.COMPLETION, runningService);
                        } else {
                            runningService.moveToNextRouteStop(timeToNext, simulatorTime);
                            log.info("runningService : " + runningService.getForLog() + " moved to " + runningService.getLastBerth());
                            sendRunningServiceMessage(MessageType.MOVEMENT, runningService);
                        }
                    } else {
                        log.info("Waiting for shcedule " + runningService.getForLog());
                    }
                }
            } else {
                // Time TO WAIT FOR NEXT SCHEDULED MOVE
                log.info("Service " + runningService.getStartTime() + " from " + runningService.getOrigin() + " loop wait.");
            }
            try {
                Thread.sleep(Duration.ofSeconds(loopDelay));
            } catch (InterruptedException e) {
                log.error("runningService " + runningService.getOrigin() + " (" + runningService.getStartTime() + ") thread interrupt", e);
            }
        }
    }

    private LocalTime calculatetimeRequiredToNextPassOrStop(RunningService runningService) {
        // Find the time that the service should be at the next routepoint.
        RunningRouteStop stop = runningService.getRouteStops()[runningService.getCurrentRouteStop()];
        double lengthOfBerth = stop.getBerth().getLengthMiles();
        int speedLimit = stop.getBerth().getSpeedLimit();
        double timeToTransit = 60 * lengthOfBerth / (double) (speedLimit);
        return stop.getActualArrivalTime().plusMinutes((int) timeToTransit);

    }

    private void sendRunningServiceMessage(MessageType type, RunningService runningService) {
        RailSimMessage<RunningService> msg = new RailSimMessage<>();
        msg.setMsgKey(UUID.randomUUID());
        msg.setMessageType(type);
        msg.setMessageBody(runningService);
        log.info("Runner broadcast Service = " + runningService.getStartTime() + " from " + runningService.getOrigin() +
                " Msg type " + type + " Current berth " + runningService.getRouteStops()[runningService.getCurrentRouteStop()]);
        messageBroadcaster.broadcast(msg);
    }


    @Override
    public void onMessage(RailSimMessage<?> message) {
        if(message.getMessageType() == MessageType.STATUS) {
            simulatorTime = ((SystemStatus) message.getMessageBody()).simulatorTime();
            receivingMessages = true;
        }
    }

    @Override
    public Optional<UI> retrieveUI() {
        return Optional.empty();
    }
}
