package uk.co.raphel.trainsimserver.railsimRunner;


import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.trainsimserver.service.DashboardBroadcaster;

import java.util.UUID;

public class RailsimRunnerTask implements Runnable{

    private final TrainService trainService;
    private final DashboardBroadcaster dashboardBroadcaster;

    public RailsimRunnerTask(TrainService trainService,  DashboardBroadcaster dashboardBroadcaster) {
        this.trainService = trainService;
        this.dashboardBroadcaster = dashboardBroadcaster;
    }

    public void run() {
        RailSimMessage<TrainService> msg = new RailSimMessage<>();
        msg.setMsgKey(UUID.randomUUID());
        msg.setMessageType(MessageType.SERVICESTART);
        msg.setMessageBody(trainService);
        dashboardBroadcaster.broadcast(msg);

    }
}
