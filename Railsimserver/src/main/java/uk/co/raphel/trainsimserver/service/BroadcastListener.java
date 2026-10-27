package uk.co.raphel.trainsimserver.service;

import uk.co.raphel.railsim.common.nats.RailSimMessage;

@Deprecated
public interface BroadcastListener {

    void onMessage(RailSimMessage<?> message);

}
