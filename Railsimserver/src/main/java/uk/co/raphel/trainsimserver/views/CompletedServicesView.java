package uk.co.raphel.trainsimserver.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.trainsimserver.service.MessageBroadcaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//@Route(value = "Completed", layout = MainLayout.class)
//@UIScope
@Slf4j(topic = "CompletedServicesView")
public class CompletedServicesView
        extends AbstractPanel<RailSimMessage<?>> {

    private static final int NUMBER_OF_PAST_TRAINS = 5;

    private final MessageBroadcaster broadcaster;

    private ListDataProvider<TrainService> dataProvider;
    private List<TrainService> serviceList;

    public CompletedServicesView(MessageBroadcaster broadcaster) {
        super(broadcaster);
        this.broadcaster = broadcaster;

        createCompletedView();
    }

    private void createCompletedView() {

        setHeadings("Completed");

        Grid<TrainService> scheduleList = new Grid<>();

        scheduleList.addColumn(TrainService::getStartTime)
                .setHeader("Start Time");

        scheduleList.addColumn(TrainService::getOrigin)
                .setHeader("Starting At");

        scheduleList.addColumn(TrainService::getDestination)
                .setHeader("Destination");

        scheduleList.addColumn(TrainService::getServiceClass)
                .setHeader("Class");

        scheduleList.addColumn(TrainService::getEngine)
                .setHeader("Engine");

        scheduleList.addColumn(TrainService::getTerminalTime)
                .setHeader("Arrived");

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

        super.onMessage(message); // Handle clock messages

        if (!(message instanceof RailSimMessage<?> railSimMessage)) {
            return;
        }

        if (railSimMessage.getMessageType() != MessageType.COMPLETION) {
            return;
        }

        if (!(railSimMessage.getMessageBody() instanceof List<?>)) {
            log.warn(
                    "Received COMPLETION message without a TrainService body");
            return;
        }

        UI ui = retrieveUI().orElse(null);

        if (ui == null) {
            return;
        }

        List<?> services = (List<?>) railSimMessage.getMessageBody();

        ui.access(() -> {

            for (Object service : services) {

                if (service instanceof TrainService trainService) {
                    addService(trainService);
                }
            }
        });
    }

    /**
     * Adds a new completed service or updates an existing service.
     *
     * The TrainService ID is used to determine whether the service
     * already exists in the Grid.
     */
    private void addService(TrainService trainService) {

        for (int i = 0; i < serviceList.size(); i++) {

            TrainService existing = serviceList.get(i);

            if (Objects.equals(
                    existing.getId(),
                    trainService.getId())) {

                // Existing service - replace it with the updated version.
                serviceList.set(i, trainService);

                dataProvider.refreshAll();
                return;
            }
        }

        // New completed service.
        if (serviceList.size() >= NUMBER_OF_PAST_TRAINS) {
            serviceList.removeFirst();
        }

        serviceList.add(trainService);

        dataProvider.refreshAll();
    }
}
