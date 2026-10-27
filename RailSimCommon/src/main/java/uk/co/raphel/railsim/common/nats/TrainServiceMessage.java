package uk.co.raphel.railsim.common.nats;

import lombok.Getter;
import lombok.Setter;
import uk.co.raphel.broadcast.api.BroadcastMessageType;
import uk.co.raphel.railsim.common.dto.RunningService;
import uk.co.raphel.railsim.common.enums.MessageType;

import java.util.UUID;

@Getter @Setter
@BroadcastMessageType( "railsim.trainservice")
public class TrainServiceMessage extends RailSimMessage<RunningService>   {


    private MessageType messageType ;

    // For jackson
    public TrainServiceMessage() {
        super();
    }

    public TrainServiceMessage(UUID msgKey, RunningService service, MessageType messageType) {
        super(msgKey, service);
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return messageBody == null ? "N/A" : asString(messageBody) ;
    }

    @Override
    public RunningService getMessageBody() {
        return messageBody;
    }

    private String asString(RunningService service) {
        return service.toString();

    }
}

