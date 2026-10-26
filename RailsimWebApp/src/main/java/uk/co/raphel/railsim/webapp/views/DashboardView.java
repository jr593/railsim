package uk.co.raphel.railsim.webapp.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import uk.co.raphel.railsim.common.dto.ServiceBoardEntry;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.railsim.webapp.feed.ServiceBoardListener;
import uk.co.raphel.railsim.webapp.feed.ServiceBoardStore;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Train Services")
public class DashboardView extends VerticalLayout implements ServiceBoardListener {

    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

    private final ServiceBoardStore store;
    private final List<ServiceBoardEntry> serviceList = new ArrayList<>();
    private final ListDataProvider<ServiceBoardEntry> dataProvider;

    public DashboardView(ServiceBoardStore store) {
        this.store = store;

        Grid<ServiceBoardEntry> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

        Grid.Column<ServiceBoardEntry> from = grid.addColumn(ServiceBoardEntry::origin).setHeader("From");
        Grid.Column<ServiceBoardEntry> departure = timeColumn(grid, ServiceBoardEntry::departure, "Departure");
        Grid.Column<ServiceBoardEntry> destination = grid.addColumn(ServiceBoardEntry::destination).setHeader("Destination");
        Grid.Column<ServiceBoardEntry> schedArrival = timeColumn(grid, ServiceBoardEntry::scheduledArrival, "Sched Arrival");

        Grid.Column<ServiceBoardEntry> stopNumber = grid.addColumn(ServiceBoardEntry::currentStopNumber)
                .setHeader("Stop No.")
                .setTooltipGenerator(e -> Objects.toString(e.currentStop(), ""))
                .setTextAlign(ColumnTextAlign.END);
        Grid.Column<ServiceBoardEntry> stopSchedArr = timeColumn(grid, ServiceBoardEntry::stopScheduledArrival, "Sched Arr");
        Grid.Column<ServiceBoardEntry> stopActArr = timeColumn(grid, ServiceBoardEntry::stopActualArrival, "Act Arr");
        Grid.Column<ServiceBoardEntry> stopSchedDep = timeColumn(grid, ServiceBoardEntry::stopScheduledDeparture, "Sched Dep");
        Grid.Column<ServiceBoardEntry> stopActDep = timeColumn(grid, ServiceBoardEntry::stopActualDeparture, "Actual Dep");

        Grid.Column<ServiceBoardEntry> nextStop = grid.addColumn(e -> Objects.toString(e.nextStop(), "—"))
                .setHeader("Next Stop");
        Grid.Column<ServiceBoardEntry> nextSchedArr = timeColumn(grid, ServiceBoardEntry::nextStopScheduledArrival, "Sched Arrival");

        HeaderRow groups = grid.prependHeaderRow();
        groups.join(from, departure, destination, schedArrival).setText("Service");
        groups.join(stopNumber, stopSchedArr, stopActArr, stopSchedDep, stopActDep).setText("Current Stop");
        groups.join(nextStop, nextSchedArr).setText("Next Stop");

        grid.setPartNameGenerator(e -> e.blocked() ? "blocked-row" : null);

        // Identify rows by serviceId so refreshItem() works when a row is
        // replaced by a newer snapshot of the same service.
        dataProvider = new ListDataProvider<>(serviceList) {
            @Override
            public Object getId(ServiceBoardEntry item) {
                return item.serviceId();
            }
        };
        grid.setDataProvider(dataProvider);
        grid.setSizeFull();

        setSizeFull();
        add(grid);
    }

    private static Grid.Column<ServiceBoardEntry> timeColumn(Grid<ServiceBoardEntry> grid,
                                                             ValueProvider<ServiceBoardEntry, LocalTime> time,
                                                             String header) {
        return grid.addColumn(e -> format(time.apply(e))).setHeader(header);
    }

    private static String format(LocalTime time) {
        return time == null ? "—" : df.format(time);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        serviceList.clear();
        serviceList.addAll(store.getServices());
        dataProvider.refreshAll();
        store.register(this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        store.unregister(this);
        super.onDetach(detachEvent);
    }

    // ServiceBoardStore invokes these inside ui.access(), so the grid's list can be modified directly.
    @Override
    public void onServiceUpdate(MessageType type, ServiceBoardEntry entry) {
        int idx = indexOf(entry.serviceId());
        if (type == MessageType.COMPLETION) {
            if (idx >= 0) {
                serviceList.remove(idx);
                dataProvider.refreshAll();
            }
        } else if (idx >= 0) {
            serviceList.set(idx, entry);
            dataProvider.refreshItem(entry);
        } else {
            serviceList.add(entry);
            dataProvider.refreshAll();
        }
    }

    @Override
    public void onConnectionChanged(boolean connected) {
        if (!connected) {
            serviceList.clear();
            dataProvider.refreshAll();
        }
    }

    private int indexOf(Long serviceId) {
        for (int i = 0; i < serviceList.size(); i++) {
            if (Objects.equals(serviceList.get(i).serviceId(), serviceId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Optional<UI> retrieveUI() {
        return getUI();
    }
}
