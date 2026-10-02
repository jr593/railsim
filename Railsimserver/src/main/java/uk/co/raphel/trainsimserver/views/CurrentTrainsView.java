package uk.co.raphel.trainsimserver.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.trainsimserver.service.BroadcastListener;
import uk.co.raphel.trainsimserver.service.MessageBroadcaster;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//@Route(value = "Running", layout = MainLayout.class)
//@UIScope
@Slf4j(topic = "CurrentTrainsView")
public class CurrentTrainsView extends AbstractPanel<List<TrainService>> {

    private static final int MAX_NUMBER_RUNNERS = 99;

    private final MessageBroadcaster broadcaster;

    private Grid<RunningService> runningServicesGrid;
    private List<RunningService> serviceList;
    private ListDataProvider<RunningService> dataProvider;

    public CurrentTrainsView(MessageBroadcaster broadcaster) {
        super(broadcaster);
        this.broadcaster = broadcaster;

        createCurrentRunnersView();
    }

    private void createCurrentRunnersView() {

        setHeadings("Running Services");

        runningServicesGrid = new Grid<>();

        runningServicesGrid.addColumn(RunningService::getStartTime)
                .setHeader("Departure Time");

        runningServicesGrid.addColumn(RunningService::getOrigin)
                .setHeader("Starting At");

        runningServicesGrid.addColumn(RunningService::getDestination)
                .setHeader("Destination");

        runningServicesGrid.addColumn(RunningService::getScheduledArrivalTime)
                .setHeader("Arrival Time");

        runningServicesGrid.addColumn(RunningService::getLastBerth)
                .setHeader("Current Berth");

        runningServicesGrid.addColumn(RunningService::getNextBerth)
                .setHeader("Next Berth");

        runningServicesGrid.addColumn(RunningService::getNextBerthDueAt)
                .setHeader("Due At");

        serviceList = new ArrayList<>();

        dataProvider = new ListDataProvider<>(serviceList);
        runningServicesGrid.setDataProvider(dataProvider);

        setSizeFull();
        runningServicesGrid.setSizeFull();

        add(runningServicesGrid);
    }

    @Override
    public void onMessage(RailSimMessage<?> message) {

        if (message == null) {
            return;
        }

        super.onMessage(message);

        UI ui = retrieveUI().orElse(null);

        if (ui == null) {
            return;
        }

        /*
         * All changes to the Grid's backing list must happen inside
         * the UI access block.
         */
        ui.access(() -> processMessage(message));
    }

    private void processMessage(RailSimMessage<?> message) {

        Object body = message.getMessageBody();

        if (body instanceof List<?>) {

            for (Object item : (List<?>) body) {

                if (item instanceof RunningService service) {
                    processServiceMessage(message.getMessageType(), service);
                }
            }

            return;
        }

        if (body instanceof RunningService service) {
            processServiceMessage(message.getMessageType(), service);
        }
    }

    private void processServiceMessage(
            MessageType messageType,
            RunningService service) {

        switch (messageType) {

            case SERVICESTART:
                addService(service);
                break;

            case COMPLETION:
                removeService(service);
                break;

            case BLOCKING:
                markTrainBlockingSection(service);
                break;

            case MOVEMENT:
                trackTrainService(service);
                break;

            case SCHEDULE:
                // Schedule messages are handled by NextDeparturesView.
                break;

            case ERROR:
                log.warn("Received error msg in stream");
                break;

            default:
                break;
        }
    }

    /**
     * Adds a new service or updates an existing service.
     *
     * ServiceId is used to determine whether the service already exists.
     */
    private void addService(RunningService trainService) {

        for (int i = 0; i < serviceList.size(); i++) {

            RunningService existing = serviceList.get(i);

            if (Objects.equals(
                    existing.getServiceId(),
                    trainService.getServiceId())) {

                // Existing service - replace it with the updated version.
                serviceList.set(i, trainService);

                dataProvider.refreshAll();
                return;
            }
        }

        // New service.
        if (serviceList.size() >= MAX_NUMBER_RUNNERS) {
            serviceList.removeFirst();
        }

        serviceList.add(trainService);

        dataProvider.refreshAll();
    }

    /**
     * Updates an existing service when it completes.
     */
    private void removeService(RunningService trainService) {

        RunningService existing = findService(trainService.getServiceId());

        if (existing == null) {
            log.error(
                    "Unable to find train service {} in running services.",
                    trainService.getServiceId());
            return;
        }

        existing.setBlocked(false);
        existing.setActualArrivalTime(simulatorTime);
        existing.setTerminated(true);

        dataProvider.refreshAll();
    }

    /**
     * Marks an existing service as blocked.
     */
    private void markTrainBlockingSection(RunningService trainService) {

        RunningService existing = findService(trainService.getServiceId());

        if (existing == null) {
            log.error(
                    "Unable to find train service {} in running services.",
                    trainService.getServiceId());
            return;
        }

        existing.setBlocked(true);

        dataProvider.refreshAll();
    }

    /**
     * Handles movement updates.
     *
     * If movement messages contain the complete updated RunningService,
     * replace the existing service with the new version.
     */
    private void trackTrainService(RunningService trainService) {

        for (int i = 0; i < serviceList.size(); i++) {

            RunningService existing = serviceList.get(i);

            if (Objects.equals(
                    existing.getServiceId(),
                    trainService.getServiceId())) {

                serviceList.set(i, trainService);

                dataProvider.refreshAll();
                return;
            }
        }

        log.warn(
                "Received movement for unknown train service {}",
                trainService.getServiceId());
    }

    /**
     * Finds a service by ServiceId.
     */
    private RunningService findService(Long serviceId) {

        for (RunningService service : serviceList) {

            if (Objects.equals(service.getServiceId(), serviceId)) {
                return service;
            }
        }

        return null;
    }

    /**
     * Removes the oldest service from the top of the Grid
     * without adding a replacement.
     */
    public void removeOldestService() {

        UI ui = retrieveUI().orElse(null);

        if (ui == null) {
            return;
        }

        ui.access(() -> {

            if (serviceList.isEmpty()) {
                return;
            }

            serviceList.removeFirst();

            dataProvider.refreshAll();
        });
    }
}
