package uk.co.raphel.railsim.webapp.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import uk.co.raphel.railsim.common.dto.ServiceBoardEntry;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.railsim.webapp.feed.ServiceBoardListener;
import uk.co.raphel.railsim.webapp.feed.ServiceBoardStore;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainLayout extends AppLayout implements ServiceBoardListener {

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

    private final ServiceBoardStore store;
    private final Span connectionStatus = new Span();
    private final Span systemClock = new Span("--:--");

    public MainLayout(ServiceBoardStore store) {
        this.store = store;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 title = new H1("Railsim Dashboard");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)");

        systemClock.addClassName("sim-clock");
        connectionStatus.addClassName("connection-status");

        addToNavbar(new DrawerToggle(), title, systemClock, connectionStatus);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Train Services", DashboardView.class, VaadinIcon.TRAIN.create()));
        addToDrawer(nav);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        onClockTick(store.getSimulatorTime());
        onConnectionChanged(store.isConnected());
        store.register(this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        store.unregister(this);
        super.onDetach(detachEvent);
    }

    @Override
    public void onServiceUpdate(MessageType type, ServiceBoardEntry entry) {
        // Services are shown by DashboardView
    }

    @Override
    public void onClockTick(LocalTime simulatorTime) {
        systemClock.setText(simulatorTime == null ? "--:--" : df.format(simulatorTime));
    }

    @Override
    public void onConnectionChanged(boolean connected) {
        connectionStatus.setText(connected ? "Connected" : "Server offline");
        connectionStatus.getStyle().set("color",
                connected ? "var(--lumo-success-text-color)" : "var(--lumo-error-text-color)");
    }

    @Override
    public Optional<UI> retrieveUI() {
        return getUI();
    }
}
