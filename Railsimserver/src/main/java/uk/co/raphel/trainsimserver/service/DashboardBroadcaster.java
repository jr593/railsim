package uk.co.raphel.trainsimserver.service;

import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.DashboardMessage;
import uk.co.raphel.trainsimserver.views.DashboardView;

import java.util.List;
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
public class DashboardBroadcaster {

    private final List<DashboardView> listeners =
            new CopyOnWriteArrayList<>();

    /**
     * Register a dashboard for updates.
     */
    public void register(DashboardView view) {

        if (listeners.contains(view)) {
            return;
        }

        listeners.add(view);

        view.getUI().ifPresent(ui ->
                ui.addDetachListener(event ->
                        listeners.remove(view)
                )
        );
    }

    /**
     * Send an update to every connected dashboard.
     */
    public void broadcast(Object messge) {

        for (DashboardView view : listeners) {

            view.getUI().ifPresent(ui -> {

                ui.access(() -> {
                    view.sendDataToClient(messge);
                });

            });
        }
    }
}