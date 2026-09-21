package uk.co.raphel.trainsimserver.railsimRunner;

import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.trainsimserver.service.DashboardBroadcaster;

import java.time.LocalTime;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.co.raphel.trainsimserver.TrainUtils.routePointToRouteStop;

public class RailsimRunnerTask implements Runnable{

    private final TrainService trainService;
    private final DashboardBroadcaster dashboardBroadcaster;

    public RailsimRunnerTask(TrainService trainService,  DashboardBroadcaster dashboardBroadcaster) {
        this.trainService = trainService;
        this.dashboardBroadcaster = dashboardBroadcaster;
    }

    public void run() {
        RailSimMessage<RunningService> msg = new RailSimMessage<>();
        msg.setMsgKey(UUID.randomUUID());
        msg.setMessageType(MessageType.SERVICESTART);

        RunningService tracker = new RunningService();
        tracker.setDestination(trainService.getDestination());
        tracker.setActualStartTime(LocalTime.now());
        tracker.setOrigin(trainService.getOrigin());
        tracker.setRouteStops(trainService.getRoutePoints().stream().map(st -> routePointToRouteStop(st)).collect(Collectors.toList()));
        msg.setMessageBody(tracker);
        dashboardBroadcaster.broadcast(msg);



        // NMeed to send MOVEMENT when we move the service
    }


}
