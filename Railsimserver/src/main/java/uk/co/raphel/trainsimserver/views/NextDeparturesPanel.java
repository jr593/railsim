package uk.co.raphel.trainsimserver.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.trainsimserver.service.BroadcastListener;
import uk.co.raphel.trainsimserver.service.DashboardBroadcaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class NextDeparturesPanel extends AbstractPanel<List<TrainService>> {

    private static final int MAX_NUMBER_SCHEDULES = 3;

     private final Grid<TrainService> scheduleList;
    private final List<TrainService> serviceList;
    private final ListDataProvider<TrainService> dataProvider;

    public NextDeparturesPanel(DashboardBroadcaster broadcaster) {

        super(broadcaster);

        scheduleList = new Grid<>();

        scheduleList.addColumn(TrainService::getStartTime)
                .setHeader("Start Time");

        scheduleList.addColumn(TrainService::getOrigin)
                .setHeader("Starting At");

        scheduleList.addColumn(t -> t.getRoutePoints().size())
                .setHeader("Number Stops");

        scheduleList.addColumn(TrainService::getServiceClass)
                .setHeader("Class");

        scheduleList.addColumn(TrainService::getEngine)
                .setHeader("Engine");

        serviceList = new ArrayList<>();

        dataProvider = new ListDataProvider<>(serviceList);
        scheduleList.setDataProvider(dataProvider);

        setSizeFull();
        scheduleList.setSizeFull();

        add(scheduleList);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        broadcaster.register(this);
    }

    @Override
    public void onMessage(RailSimMessage<?> message) {

        if (!(message instanceof RailSimMessage<?> railSimMessage)) {
            return;
        }

        if (railSimMessage.getMessageType() != MessageType.SCHEDULE) {
            return;
        }

        if (!(railSimMessage.getMessageBody() instanceof List)) {
            log.warn("Received SCHEDULE message without a TrainService body");
            return;
        }

        UI ui = retrieveUI().orElse(null);

        if (ui == null) {
            return;
        }
        List<TrainService> services = (List<TrainService>)railSimMessage.getMessageBody();
        for(TrainService trainService : services) {
            ui.access(() -> addService(trainService));
        }
    }

    /**
     * Adds a new service to the bottom of the Grid.
     *
     * If the Grid already contains the maximum number of services,
     * the oldest service at the top is removed first.
     */
    private void addService(TrainService trainService) {

        if (serviceList.size() >= MAX_NUMBER_SCHEDULES) {
            serviceList.removeFirst();
        }

        serviceList.add(trainService);

        dataProvider.refreshAll();
    }

    /**
     * Removes the oldest service from the top of the Grid
     * without adding a replacement.
     */
    public void removeOldestService() {

        if (serviceList.isEmpty()) {
            return;
        }

        serviceList.removeFirst();

        dataProvider.refreshAll();
    }

    @Override
    public Optional<UI> retrieveUI() {
        return this.getUI();
    }

}
