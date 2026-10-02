package uk.co.raphel.trainsimserver.service;

import com.vaadin.flow.component.UI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.RailSimMessage;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
/*The important bit is:

        ui.access(() -> {
        view.update(value);
});

Your background code must not directly modify Vaadin components.

        UI.access() schedules the update on the Vaadin UI thread.

Because you have @Push, the browser then receives the changed state.
*/

@Service
@Slf4j(topic = "MessageBroadcaster")
public class MessageBroadcaster {

    private final List<BroadcastListener> listeners =
            new CopyOnWriteArrayList<>();

    /**
     * Register a dashboard for updates.
     */
    public void register(BroadcastListener listener) {

        if (listeners.contains(listener)) {
            return;
        }

        listeners.add(listener);

        listener.retrieveUI().ifPresent(ui ->
                ui.addDetachListener(event ->
                        listeners.remove(listener)
                )
        );
    }

    /**
     * Send an update to every connected View.
     */
    public void broadcast(RailSimMessage<?> msg) {

        //log.info("Msg Type = " + messge.getClass().getName());
        if(msg.getMessageBody() == null ) {
            log.warn("WHOOPS");
        }
        for (BroadcastListener listener : listeners) {

            Optional<UI> hasUi = listener.retrieveUI();
            if(hasUi.isPresent()) {
                hasUi.get().access(() -> {
                    listener.onMessage(msg);
                });
            } else {
                listener.onMessage(msg);
            }

        }
    }

}