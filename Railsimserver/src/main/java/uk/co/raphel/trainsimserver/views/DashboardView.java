package uk.co.raphel.trainsimserver.views;


import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.raphel.trainsimserver.service.DashboardBroadcaster;


@Route("")
//@Push
@UIScope
public class DashboardView extends AppLayout {

    private final Div ordersTile = new Div();

    private final DashboardBroadcaster broadcaster;

    @Autowired
    public DashboardView(DashboardBroadcaster broadcaster) {
        this.broadcaster = broadcaster;

        createHeader();
        createMenu();
        createDashboard();
    }

    private void createHeader() {
        addToNavbar(ordersTile);
    }

    private void createMenu() {
        // Add menu items here
    }

    private void createDashboard() {
        // Add dashboard content here
    }

    /** Safe update called by broadcaster */
    public void update(String value) {
        ordersTile.setText(value);
    }

    /** Register UI with broadcaster when attached */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        broadcaster.register(getUI().get());
    }
}