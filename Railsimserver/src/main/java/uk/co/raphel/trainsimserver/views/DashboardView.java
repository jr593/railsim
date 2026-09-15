package uk.co.raphel.trainsimserver.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.raphel.trainsimserver.service.DashboardBroadcaster;

@Route(value = "", layout = MainLayout.class)
@UIScope
public class DashboardView extends VerticalLayout {

    private final Div ordersTile = new Div();

    private final DashboardBroadcaster broadcaster;

    @Autowired
    public DashboardView(DashboardBroadcaster broadcaster) {
        this.broadcaster = broadcaster;

        createDashboard();
    }

    private void createDashboard() {
        H1 title = new H1("Dashboard");

        ordersTile.setText("Orders: 0");

        ordersTile.getStyle()
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("padding", "20px")
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px");

        add(title, ordersTile);
    }

    /**
     * Called by DashboardBroadcaster when an update arrives.
     */
    public void update(String value) {
        ordersTile.setText(value);
    }

    /**
     * Register this dashboard when the browser attaches.
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        broadcaster.register(this);
    }
}
