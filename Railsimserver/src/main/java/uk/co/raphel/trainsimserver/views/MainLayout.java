package uk.co.raphel.trainsimserver.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createMenu();
    }

    private void createHeader() {
        H1 title = new H1("Train Simulator");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)");

        addToNavbar(title);
    }

    private void createMenu() {
        Span menuTitle = new Span("Navigation");

        RouterLink dashboard = new RouterLink("Dashboard", DashboardView.class);
        addToDrawer(new VerticalLayout(menuTitle,  dashboard));
    }
}