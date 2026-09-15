package uk.co.raphel.trainsimserver.service;


import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;
import uk.co.raphel.trainsimserver.views.DashboardView;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DashboardBroadcaster {

    private final List<UI> listeners = new CopyOnWriteArrayList<>();

    /** Register a UI for updates */
    public void register(UI ui) {
        listeners.add(ui);
        ui.addDetachListener(event -> listeners.remove(ui));
    }

    /** Push update to all registered UIs */
    public void broadcast(String value) {
        for (UI ui : listeners) {
            ui.access(() -> {
                ui.getChildren()
                  .filter(c -> c instanceof DashboardView)
                  .map(c -> (DashboardView) c)
                  .forEach(view -> view.update(value));
            });
        }
    }
}