package uk.co.raphel.trainsimserver.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import uk.co.raphel.railsim.common.dto.RailSimMessage;
import uk.co.raphel.railsim.common.dto.SystemStatus;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.trainsimserver.service.BroadcastListener;
import uk.co.raphel.trainsimserver.service.MessageBroadcaster;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public abstract class AbstractPanel<E> extends VerticalLayout implements BroadcastListener {

    protected final MessageBroadcaster broadcaster;
    ListDataProvider<E> dataProvider;
    //private final Div systemClock = new Div();

    protected LocalTime simulatorTime;
    private DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");


    public void onMessage(RailSimMessage<?> message) {
        if (message != null && message.getMessageType().equals(MessageType.STATUS)) {
            simulatorTime = ((SystemStatus) message.getMessageBody()).simulatorTime();

    //        systemClock.setText(df.format(simulatorTime));
         }
    }

    protected AbstractPanel(MessageBroadcaster broadcaster) {
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

    public void setHeadings(String viewTitle) {
        H1 title = new H1(viewTitle);

//        systemClock.setText("");
//
//        systemClock.getStyle()
//                .set("font-size", "24px")
//                .set("font-weight", "bold")
//                .set("padding", "20px")
//                .set("border", "1px solid #ccc")
//                .set("border-radius", "8px");

        add(title);
    }
}
