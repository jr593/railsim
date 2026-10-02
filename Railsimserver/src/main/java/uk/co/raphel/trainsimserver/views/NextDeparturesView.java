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
import uk.co.raphel.trainsimserver.service.MessageBroadcaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//@Route(value = "Scheduled", layout = MainLayout.class)
//@UIScope
@Slf4j
public class NextDeparturesView extends AbstractPanel<List<TrainService>> {


    private static final int MAX_NUMBER_SCHEDULES = 10;
    private final MessageBroadcaster broadcaster;

     private  Grid<RunningService> scheduleList;
    private  List<RunningService> serviceList;
    private  ListDataProvider<RunningService> dataProvider;

    public NextDeparturesView(MessageBroadcaster broadcaster) {

        super(broadcaster);
        this.broadcaster = broadcaster;

        createSchedulesView();
    }

    private void createSchedulesView(){
        setHeadings("Upcoming Departures");

        scheduleList = new Grid<>();

        scheduleList.addColumn(RunningService::getStartTime)
                .setHeader("Start Time");

        scheduleList.addColumn(RunningService::getOrigin)
                .setHeader("Starting At");

        scheduleList.addColumn(RunningService::getDestination)
                .setHeader("Destination");

        scheduleList.addColumn(t -> t.getRouteStops().length)
                .setHeader("Number Stops");

        scheduleList.addColumn(RunningService::getServiceClass)
                .setHeader("Class");

        scheduleList.addColumn(RunningService::getEngine)
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
            super.onMessage(message);

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
        ui.access(() -> {
            List<RunningService> newServices =
                    (List<RunningService>) railSimMessage.getMessageBody();

            serviceList.clear();
            serviceList.addAll(newServices);

            dataProvider.refreshAll();
        });

//         ui.access(() -> {
//            for (RunningService trainService : (List<RunningService>)railSimMessage.getMessageBody()) {
//                addService(trainService);
//            }
//            dataProvider.refreshAll();
//        });
    }

    /**
     * Adds a new service to the bottom of the Grid.
     *
     * If the Grid already contains the maximum number of services,
     * the oldest service at the top is removed first.
     */
    private void addService(RunningService trainService) {

        for (int i = 0; i < serviceList.size(); i++) {

            RunningService existing = serviceList.get(i);

            if (existing.getServiceId() == trainService.getServiceId()) {
                // Existing service: update it in place
                serviceList.set(i, trainService);
                dataProvider.refreshAll();
            } else {
                // Maintain list
                if (serviceList.size() >= MAX_NUMBER_SCHEDULES) {
                    serviceList.removeFirst();
                }

                serviceList.add(trainService);
            }
        }
        dataProvider.refreshAll();
    }

   }
