package uk.co.raphel.trainsimserver.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.StatusMessage;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.trainsimserver.service.DashboardBroadcaster;

/*
This is responsible for the stuff that stays around while you navigate between pages.
3. DashboardView.java

This is the actual dashboard.
Notice that DashboardView doesn't extend AppLayout anymore.

It extends:

VerticalLayout

because MainLayout is now responsible for the AppLayout.

 */
@Route(value = "", layout = MainLayout.class)
@UIScope
public class DashboardView extends VerticalLayout {

    private final Div statusTile = new Div();

    private NextDeparturesPanel nextDeparturesPanel;
    private CurrentTrainsPanel currentTrainsPanel;
    private CompletedServicesPanel completedServicesPanel;

    private final DashboardBroadcaster broadcaster;

    @Autowired
    public DashboardView(DashboardBroadcaster broadcaster) {
        this.broadcaster = broadcaster;

        createDashboard();
    }

    private void createDashboard() {
        H1 title = new H1("Dashboard");

        statusTile.setText("");

        statusTile.getStyle()
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("padding", "20px")
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px");

        add(title, statusTile);
        HorizontalLayout panels = new HorizontalLayout(nextDeparturesPanel, currentTrainsPanel, completedServicesPanel);

    }

    /**
     * Called by DashboardBroadcaster when an update arrives.
     */
    public void sendDataToClient(Object message) {
        if(message instanceof StatusMessage) {
            statusTile.setText(((StatusMessage)message).getMessageBody());
        }
        if(message instanceof RailSimMessage) {
            switch(((RailSimMessage) message).getMessageType()) {
                case ERROR -> statusTile.setText(((RailSimMessage)message).getMiscInfo());
                case BLOCKING ->  ;
                case MOVEMENT -> ;
                case SCHEDULE -> ;
                case COMPLETION -> ;
                case SERVICESTART -> currentTrainsPanel.addService(((RailSimMessage<TrainService>) message).getMessageBody());

            }
        }
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
