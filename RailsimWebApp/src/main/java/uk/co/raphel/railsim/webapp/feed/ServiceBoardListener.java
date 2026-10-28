package uk.co.raphel.railsim.webapp.feed;

import com.vaadin.flow.component.UI;
import uk.co.raphel.railsim.common.dto.ServiceBoardEntry;
import uk.co.raphel.railsim.common.enums.MessageType;

import java.time.LocalTime;
import java.util.Optional;

@Deprecated
public interface ServiceBoardListener {

    void onServiceUpdate(MessageType type, ServiceBoardEntry entry);

    default void onClockTick(LocalTime simulatorTime) {
    }

    /** Called when the SSE link to Railsimserver goes up or down. On down, all services are cleared. */
    default void onConnectionChanged(boolean connected) {
    }

    Optional<UI> retrieveUI();
}
