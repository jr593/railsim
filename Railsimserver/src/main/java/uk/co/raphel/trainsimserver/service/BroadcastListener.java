package uk.co.raphel.trainsimserver.service;

import uk.co.raphel.railsim.common.dto.RailSimMessage;

public interface BroadcastListener {

    void onMessage(RailSimMessage<?> message);

}
