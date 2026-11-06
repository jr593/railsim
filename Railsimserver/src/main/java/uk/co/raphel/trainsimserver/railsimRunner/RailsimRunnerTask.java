package uk.co.raphel.trainsimserver.railsimRunner;

import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.railsim.common.dto.*;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.railsim.common.enums.ServiceEventType;
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

    private volatile boolean receivingMessages ;

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

    public RailsimRunnerTask(TrainService trainService, MessageBroadcaster messageBroadcaster,
                             TrackManagerService trackManagerService, TrainManagerService trainMonitor) {
        this.runningService = RunningService.from(trainService);
        this.messageBroadcaster = messageBroadcaster;
        this.trackManagerService = trackManagerService;
        this.trainMonitor = trainMonitor;
        receivingMessages = false;
    }

    /**
     * Process one Train Service.
     * This method handles the lifecycle of a train service, from start to completion.
     * It registers itself as a message listener, initializes the train service's start time and status,
     * and then enters a loop to process service events such as departure, arrival at stops, blocking,
     * and completion.
     * The method also handles thread synchronization and ensures proper cleanup by unregistering itself
     * as a message listener before exiting.
     */
    // Process one Train Service
    public void run() {
        messageBroadcaster.register(this);
        try {
            RailSimMessage<RunningService> msg = new RailSimMessage<>();
            msg.setMsgKey(UUID.randomUUID());
            msg.setMessageType(MessageType.SERVICESTART);

            //runningService.setDestination(runningService.getDestination());
            runningService.setActualStartTime(simulatorTime);
            runningService.setLatestStatus(ServiceEventType.STARTING);
            trainMonitor.accept(runningService,ServiceEventType.STARTING);

            runningService.setCurrentRouteStop(0);
            // we 'arrive' at the origin as we start.
            runningService.getRouteStops()[runningService.getCurrentRouteStop()].setActualArrivalTime(runningService.getActualStartTime());
            msg.setMessageBody(runningService.copy());
            log.info("Created running service :{}", runningService.getForLog());
            messageBroadcaster.broadcast(msg);

            boolean atDestination = false;
        //    long iterationCount = 1;
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
            while (!atDestination && !Thread.currentThread().isInterrupted()) {
                 if (simulatorTime.isAfter(runningService.getRouteStops()[runningService.getCurrentRouteStop()].getScheduledDepartureTime())) {
                    // TIME WE MOVED
                    log.info("{} Attempting to move", runningService.getTitle());
                    if (!trackManagerService.isFree(runningService.getRouteStops()[runningService.getCurrentRouteStop() + 1])) {
                        log.info("{} blocked next section..", runningService.getTitle());
                        // WE ARE BLOCKED
                        runningService.setLatestStatus(ServiceEventType.STOPPING);
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
                        //     log.info(runningService.getTitle() +  " free to move");

                        if (runningService.isBlocked()) {
                            log.info("{} UNBLOCKED", runningService.getTitle());
                            runningService.setBlocked(false);
                        }
                        runningService.setLatestStatus(ServiceEventType.STARTING);
                     //   trainMonitor.accept(runningService, ServiceEventType.STARTING);
                        LocalTime timeToNext = calculatetimeRequiredToNextPassOrStop(runningService);
                        log.info("{} Time check simTime={} next= {}", runningService.getTitle(), df.format(simulatorTime), df.format(timeToNext));
                        if (timeToNext.isBefore(simulatorTime)) {
                            log.info("{} Time expired , move me", runningService.getTitle());
                            if (runningService.isTerminalBerth()) {
                                atDestination = true;
                                runningService.setLatestStatus(ServiceEventType.TERMINATING);
                              //  trainMonitor.accept(runningService, ServiceEventType.TERMINATING);
                                log.info("{} is FINISHED.", runningService.getTitle());
                                sendRunningServiceMessage(MessageType.COMPLETION, runningService);
                            } else {
                                runningService.moveToNextRouteStop(timeToNext, simulatorTime);
                                runningService.setLatestStatus(ServiceEventType.ARRIVESTOP);
                           //     trainMonitor.accept(runningService, ServiceEventType.ARRIVESTOP);
                                log.info("{} moved to {}", runningService.getTitle(), runningService.getLastBerth());
                                sendRunningServiceMessage(MessageType.MOVEMENT, runningService);
                            }
                        } else {
                            log.info("{}Waiting for schedule ", runningService.getTitle());
                        }
                    }
                } else {
                    // Time TO WAIT FOR NEXT SCHEDULED MOVE
                    log.info("{} waiting to move at {}", runningService.getTitle(), df.format(runningService.getRouteStops()[runningService.getCurrentRouteStop()].getScheduledDepartureTime()));
                }
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
        } finally {
            messageBroadcaster.unregister(this);
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
        RailSimMessage<RunningService> msg = new RailSimMessage<>();
        msg.setMsgKey(UUID.randomUUID());
        msg.setMessageType(type);
        // Send a snapshot: this task keeps mutating runningService while listeners render it.
        msg.setMessageBody(runningService.copy());
        log.info("Runner broadcast Service = {} from {} Msg type {} Current berth {}", runningService.getStartTime(), runningService.getOrigin(), type, runningService.getRouteStops()[runningService.getCurrentRouteStop()]);
        messageBroadcaster.broadcast(msg);
    }


    @Override
    public void onMessage(RailSimMessage<?> message) {
        if(message.getMessageType() == MessageType.STATUS) {
       //     log.info("Have simulator time");
            simulatorTime = ((SystemStatus) message.getMessageBody()).simulatorTime();
            receivingMessages = true;
        }
    }
}
