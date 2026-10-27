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
public class RailsimRunnerTask implements Runnable, BroadcastListener {

    private final RunningService runningService;
    private final MessageBroadcaster messageBroadcaster;

    // Written by the scheduler thread (onMessage), read by this task's thread.
    private volatile LocalTime simulatorTime;

    private final TrackManagerService trackManagerService;
    private final TrainManagerService trainMonitor;

    @SuppressWarnings("FieldCanBeLocal")
    private static final int loopDelay = 20;

    private volatile boolean receivingMessages;

    private DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

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
        log.info("Created running service :" + runningService.getForLog());
        messageBroadcaster.broadcast(msg);

        boolean atDestination = false;
        //    long iterationCount = 1;
        if (!receivingMessages) {
            log.info(runningService.getTitle() + " Waiting for msgs to start");
        }
        while (!receivingMessages && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                log.error(runningService.getTitle() + "Got interrupted waiting for kick off");
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.info(runningService.getTitle() + "About to go not at destination = " + atDestination);
        MessageType requiredMessageToSend = MessageType.ERROR;
        while (!atDestination && !Thread.currentThread().isInterrupted()) {
            //  log.info(runningService.getTitle() + "Entered while loop");
            //  log.info(runningService.getForLog() + " iteration " + iterationCount);
            //  log.info(runningService.getTitle() + "Sim time now " + df.format(simulatorTime) + " next departure time = " +
            //          df.format(runningService.getRouteStops()[runningService.getCurrentRouteStop()].getScheduledDepartureTime()));
            if (simulatorTime.isAfter(runningService.getRouteStops()[runningService.getCurrentRouteStop()].getScheduledDepartureTime())) {
                // TIME WE MOVED
                log.info(runningService.getTitle() + " Attempting to move");
                if (!trackManagerService.isFree(runningService.getRouteStops()[runningService.getCurrentRouteStop() + 1])) {
                    log.info(runningService.getTitle() + " blocked next section..");
                    // WE ARE BLOCKED
                    runningService.setLatestStatus(ServiceEventType.STOPPING);
                    messageBroadcaster.broadcast(new TrainServiceMessage(UUID.randomUUID(), runningService, MessageType.STATUS));
                    //    trainMonitor.accept(runningService, ServiceEventType.STOPPING);
                    if (!runningService.isBlocked()) {
                        sendRunningServiceMessage(MessageType.BLOCKING, runningService);
                        log.warn(runningService.getTitle() + " has gone BLOCKED");
                        runningService.setBlocked(true);
                    } else {
                        log.warn(runningService.getTitle() + " already blocked");
                    }
                } else {
                    // Free to progress
                    //     log.info(runningService.getTitle() +  " free to move");

                    if (runningService.isBlocked()) {
                        log.info(runningService.getTitle() + " UNBLOCKED");
                        runningService.setBlocked(false);
                    }
                    runningService.setLatestStatus(ServiceEventType.STARTING);
                    //   trainMonitor.accept(runningService, ServiceEventType.STARTING);
                    LocalTime timeToNext = calculatetimeRequiredToNextPassOrStop(runningService);
                    log.info(runningService.getTitle() + " Time check simTime=" + df.format(simulatorTime)
                            + " next= " + df.format(timeToNext));
                    if (timeToNext.isBefore(simulatorTime)) {
                        log.info(runningService.getTitle() + " Time expired , move me");
                        if (runningService.isTerminalBerth()) {
                            atDestination = true;
                            runningService.setLatestStatus(ServiceEventType.TERMINATING);
                            //  trainMonitor.accept(runningService, ServiceEventType.TERMINATING);
                            log.info(runningService.getTitle() + " is FINISHED.");
                            sendRunningServiceMessage(MessageType.COMPLETION, runningService);
                        } else {
                            runningService.moveToNextRouteStop(simulatorTime);
                            runningService.setLatestStatus(ServiceEventType.ARRIVESTOP);
                            //     trainMonitor.accept(runningService, ServiceEventType.ARRIVESTOP);
                            log.info(runningService.getTitle() + " moved to " + runningService.getLastBerth());
                            sendRunningServiceMessage(MessageType.MOVEMENT, runningService);
                        }
                    } else {
                        log.info(runningService.getTitle() + "Waiting for schedule ");
                    }
                }
            } else {
                // Time TO WAIT FOR NEXT SCHEDULED MOVE
                log.info(runningService.getTitle() +
                        " waiting to move at " + df.format(runningService.getRouteStops()[runningService.getCurrentRouteStop()].getScheduledDepartureTime()));
            }
            messageBroadcaster.broadcast(new TrainServiceMessage(UUID.randomUUID(), runningService, requiredMessageToSend));
            try {
                //   iterationCount++;
                Thread.sleep(Duration.ofSeconds(loopDelay));
            } catch (InterruptedException e) {
                log.error("runningService " + runningService.getOrigin() + " (" + runningService.getStartTime() + ") thread interrupt", e);
                Thread.currentThread().interrupt();
                return;
            }
            trainMonitor.accept(runningService);
        }

    }

    private LocalTime calculatetimeRequiredToNextPassOrStop(RunningService runningService) {
        // Find the time that the service should be at the next routepoint.
        RunningRouteStop stop = runningService.getRouteStops()[runningService.getCurrentRouteStop()];
        double lengthOfBerth = stop.getBerth().getLengthMiles();
        int speedLimit = stop.getBerth().getSpeedLimit();
        double timeToTransit = 60 * lengthOfBerth / (double) (speedLimit);
        return stop.getScheduledArrivalTime().plusMinutes((int) timeToTransit);

    }

    private void sendRunningServiceMessage(MessageType type, RunningService runningService) {
        TrainServiceMessage msg =new TrainServiceMessage(UUID.randomUUID(), runningService, type);
         // Send a snapshot: this task keeps mutating runningService while listeners render it.
        msg.setMessageBody(runningService.copy());
        log.info("Runner broadcast Service = " + runningService.getStartTime() + " from " + runningService.getOrigin() +
                " Msg type " + type + " Current berth " + runningService.getRouteStops()[runningService.getCurrentRouteStop()]);
        messageBroadcaster.broadcast(msg);
    }


    @Override
    public void onMessage(RailSimMessage<?> message) {
        if(message instanceof StatsMessage) {
            //     log.info("Have simulator time");
            simulatorTime = ((StatsMessage) message).getMessageBody();
            receivingMessages = true;
        }
    }
}
