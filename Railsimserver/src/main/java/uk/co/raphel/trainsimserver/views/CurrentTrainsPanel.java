package uk.co.raphel.trainsimserver.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.entity.TrainService;
import uk.co.raphel.trainsimserver.TrainUtils;
import uk.co.raphel.trainsimserver.service.BroadcastListener;
import uk.co.raphel.trainsimserver.service.DashboardBroadcaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j(topic = "CurrentTrainsPanel")
public class CurrentTrainsPanel extends AbstractPanel<List<TrainService>> {


    private  Grid<RunningService> runningServicesGrid;
    private final List<RunningService> serviceList; // Backing data
    private  ListDataProvider<RunningService> dataProvider;
    private static final int MAX_NUMBER_RUNNERS = 99;

    public CurrentTrainsPanel(DashboardBroadcaster broadcaster) {
        super(broadcaster);

        runningServicesGrid = new Grid<>();

        runningServicesGrid.addColumn(RunningService::getStartTime)
                        .setHeader("Departure Time");
        runningServicesGrid.addColumn(RunningService::getOrigin)
                .setHeader("Starting At");

        runningServicesGrid.addColumn(RunningService::getDestination)
                .setHeader("Destination");
        runningServicesGrid.addColumn(RunningService::getActualArrivalTime)
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
        if(message != null) {
            RailSimMessage<?> msg = (RailSimMessage<?>) message;
            switch(msg.getMessageType()) {
                case SERVICESTART : addService((TrainService) msg.getMessageBody()); break;
                case COMPLETION : removeService((TrainService)msg.getMessageBody()); break;
                case BLOCKING: markTrainBlockingSection(msg.getMessageBody()); break;
                case SCHEDULE:  break;
                case MOVEMENT: trackTrainSrevice(msg.getMessageBody()); break;
                case ERROR:
                    log.warn("Received error msg in stream {}", msg.getMiscInfo()); break;
            }
            UI ui = retrieveUI().orElse(null);

            if (ui == null) {
                return;
            }
            List<TrainService> services = (List<TrainService>)msg.getMessageBody();
            for(TrainService trainService : services) {
                ui.access(() -> addService(trainService));
            };
        }
    }



    /**
     * Adds a new service to the bottom of the Grid.
     *
     * If the Grid already contains the maximum number of services,
     * the oldest service at the top is removed first.
     */
    private void addService(TrainService trainService) {

        if (serviceList.size() >= MAX_NUMBER_RUNNERS) {
            serviceList.removeFirst();
        }

        serviceList.add(TrainUtils.trainServiceToRunningService(trainService));

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

}
