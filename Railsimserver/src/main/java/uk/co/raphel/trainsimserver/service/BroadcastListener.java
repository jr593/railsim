package uk.co.raphel.trainsimserver.service;

import com.vaadin.flow.component.UI;
import uk.co.raphel.railsim.common.dto.RailSimMessage;

import java.util.Optional;

public interface BroadcastListener {

    void onMessage(RailSimMessage<?> message);

    Optional<UI> retrieveUI();

}
