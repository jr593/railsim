package uk.co.raphel.trainsimserver.railsimRunner;

import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.railsim.common.dto.*;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.railsim.common.enums.ServiceEventType;
import uk.co.raphel.railsim.common.nats.RailSimMessage;
import uk.co.raphel.railsim.common.nats.StatsMessage;
import uk.co.raphel.railsim.common.nats.TrainServiceMessage;
import uk.co.raphel.trainsimserver.service.*;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j(topic = "RailSimRunnerTask")
public class RailsimRunnerTask implements Runnable /*, BroadcastListener*/ {

    private final RunningService runningService;
    private final MessageBroadcaster messageBroadcaster;

    // Written by the scheduler thread (onMessage), read by this task's thread.
    private volatile LocalTime simulatorTime;

    private final TrackManagerService trackManagerService;
    private final TrainManagerService trainMonitor;

    @SuppressWarnings("FieldCanBeLocal")
    private static final int loopDelay = 20;

    private volatile boolean receivingMessages;

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

    public RailsimRunnerTask(TrainService trainService, MessageBroadcaster messageBroadcaster,
                             TrackManagerService trackManagerService, TrainManagerService trainMonitor) {
        this.runningService = RunningService.from(trainService);
        this.messageBroadcaster = messageBroadcaster;
        this.trackManagerService = trackManagerService;
        this.trainMonitor = trainMonitor;
        receivingMessages = false;
    }

    // Process one Train Service
    public void run() {
        RailSimMessage<RunningService> msg = new TrainServiceMessage(UUID.randomUUID(), runningService, MessageType.SERVICESTART);

        //runningService.setDestination(runningService.getDestination());
        runningService.setActualStartTime(simulatorTime);
        runningService.setLatestStatus(ServiceEventType.STARTING);
        trainMonitor.accept(runningService, ServiceEventType.STARTING);
        //  runningService.setOrigin(runningService.getOrigin());
        //  runningService.setRouteStops(runningService.getRoutePoints().stream()
        //         .map(p ->  RunningRouteStop.from(p, runningService.getId())).toList().toArray(new RunningRouteStop[0]));
        runningService.setCurrentRouteStop(0);
        // we 'arrive' at the origin as we start.
        runningService.getRouteStops()[runningService.getCurrentRouteStop()].setActualArrivalTime(runningService.getActualStartTime());
        msg.setMessageBody(runningService.copy());
        log.info("Created running service :{}", runningService.getForLog());
        messageBroadcaster.broadcast(msg);

        boolean atDestination = false;
        if (!receivingMessages) {
            log.info("{} Waiting for msgs to start", runningService.getTitle());
        }
        while (!receivingMessages && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                log.error("{}Got interrupted waiting for kick off", runningService.getTitle());
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.info("{}About to go not at destination = {}", runningService.getTitle(), atDestination);
        MessageType requiredMessageToSend = MessageType.ERROR;
        while (!atDestination && !Thread.currentThread().isInterrupted()) {
            log.debug("{}Entered while loop", runningService.getTitle());
            log.debug("{}", runningService.getForLog());
            log.debug("{}Sim time now {} next departure time = {}", runningService.getTitle(), df.format(simulatorTime), df.format(runningService.getRouteStops()[runningService.getCurrentRouteStop()].getScheduledDepartureTime()));
            if (simulatorTime.isAfter(runningService.getRouteStops()[runningService.getCurrentRouteStop()].getScheduledDepartureTime())) {
                // TIME WE MOVED
                log.debug("{} Attempting to move", runningService.getTitle());
                if (!trackManagerService.isFree(runningService.getRouteStops()[runningService.getCurrentRouteStop() + 1])) {
                    log.debug("{} blocked next section..", runningService.getTitle());
                    // WE ARE BLOCKED
                    runningService.setLatestStatus(ServiceEventType.STOPPING);
                    messageBroadcaster.broadcast(new TrainServiceMessage(UUID.randomUUID(), runningService, MessageType.STATUS));
                    //    trainMonitor.accept(runningService, ServiceEventType.STOPPING);
                    if (!runningService.isBlocked()) {
                        sendRunningServiceMessage(MessageType.BLOCKING, runningService);
                        log.warn("{} has gone BLOCKED", runningService.getTitle());
                        runningService.setBlocked(true);
                    } else {
                        log.warn("{} already blocked", runningService.getTitle());
                    }
                } else {
                    // Free to progress
                    //     log.debug(runningService.getTitle() +  " free to move");

                    if (runningService.isBlocked()) {
                        log.debug("{} UNBLOCKED", runningService.getTitle());
                        runningService.setBlocked(false);
                    }
                    runningService.setLatestStatus(ServiceEventType.STARTING);
                    //   trainMonitor.accept(runningService, ServiceEventType.STARTING);
                    LocalTime timeToNext = calculateTimeRequiredToNextPassOrStop(runningService);
                    log.debug("{} Time check simTime={} next= {}", runningService.getTitle(), df.format(simulatorTime), df.format(timeToNext));
                    if (timeToNext.isBefore(simulatorTime)) {
                        log.debug("{} Time expired , move me", runningService.getTitle());
                        if (runningService.isTerminalBerth()) {
                            atDestination = true;
                            runningService.setLatestStatus(ServiceEventType.TERMINATING);
                            //  trainMonitor.accept(runningService, ServiceEventType.TERMINATING);
                            log.debug("{} is FINISHED.", runningService.getTitle());
                            sendRunningServiceMessage(MessageType.COMPLETION, runningService);
                        } else {
                            runningService.moveToNextRouteStop(simulatorTime);
                            runningService.setLatestStatus(ServiceEventType.ARRIVESTOP);
                            //     trainMonitor.accept(runningService, ServiceEventType.ARRIVESTOP);
                            log.debug("{} moved to {}", runningService.getTitle(), runningService.getLastBerth());
                            sendRunningServiceMessage(MessageType.MOVEMENT, runningService);
                        }
                    } else {
                        log.debug("{}Waiting for schedule ", runningService.getTitle());
                    }
                }
            } else {
                // Time TO WAIT FOR NEXT SCHEDULED MOVE
                log.debug("{} waiting to move at {}", runningService.getTitle(), df.format(runningService.getRouteStops()[runningService.getCurrentRouteStop()].getScheduledDepartureTime()));
            }
            messageBroadcaster.broadcast(new TrainServiceMessage(UUID.randomUUID(), runningService, requiredMessageToSend));
            try {
                //   iterationCount++;
                Thread.sleep(Duration.ofSeconds(loopDelay));
            } catch (InterruptedException e) {
                log.error("runningService {} ({}) thread interrupt", runningService.getOrigin(), runningService.getStartTime(), e);
                Thread.currentThread().interrupt();
                return;
            }
            trainMonitor.accept(runningService);
        }

    }

    private LocalTime calculateTimeRequiredToNextPassOrStop(RunningService runningService) {
        // Find the time that the service should be at the next routepoint.
        RunningRouteStop stop = runningService.getRouteStops()[runningService.getCurrentRouteStop()];
        double lengthOfBerth = stop.getBerth().getLengthMiles();
        int speedLimit = stop.getBerth().getSpeedLimit();
        double timeToTransit = 60 * lengthOfBerth / (double) (speedLimit);
        return stop.getScheduledArrivalTime().plusMinutes((int) timeToTransit);

    }

    private void sendRunningServiceMessage(MessageType type, RunningService runningService) {
        TrainServiceMessage msg =new TrainServiceMessage(UUID.randomUUID(), runningService, type);
        msg.setMessageBody(runningService.copy());
        log.debug("Runner broadcast Service = {} from {} Msg type {} Current berth {}", runningService.getStartTime(), runningService.getOrigin(), type, runningService.getRouteStops()[runningService.getCurrentRouteStop()]);
        messageBroadcaster.broadcast(msg);
    }


    @Override
    public void onMessage(RailSimMessage<?> message) {
        if(message instanceof StatsMessage) {
            //     log.debug("Have simulator time");
            simulatorTime = ((StatsMessage) message).getMessageBody();
            receivingMessages = true;
        }
    }
}
