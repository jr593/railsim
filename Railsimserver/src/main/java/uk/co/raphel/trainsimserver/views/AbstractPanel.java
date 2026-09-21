package uk.co.raphel.trainsimserver.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.trainsimserver.service.BroadcastListener;
import uk.co.raphel.trainsimserver.service.DashboardBroadcaster;

import java.util.Optional;

public abstract class AbstractPanel<E> extends VerticalLayout implements BroadcastListener {

    protected final DashboardBroadcaster broadcaster;
    ListDataProvider<E> dataProvider;


    protected AbstractPanel(DashboardBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        broadcaster.register(this);
    }


    @Override
    public Optional<UI> retrieveUI() {
        return this.getUI();
    }
}
