package uk.co.raphel.trainsimserver.service;

import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;
import uk.co.raphel.trainsimserver.views.DashboardView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public void broadcast(String value) {

        for (DashboardView view : listeners) {

            view.getUI().ifPresent(ui -> {

                ui.access(() -> {
                    view.update(value);
                });

            });
        }
    }
}