package uk.co.raphel.railsim.common.nats;

import lombok.Getter;
import lombok.Setter;
import uk.co.raphel.broadcast.api.BroadcastMessageType;
import uk.co.raphel.railsim.common.dto.RunningService;

import java.util.List;
import java.util.UUID;

@Getter @Setter
@BroadcastMessageType( "railsim.schedule")
@Deprecated
public class ScheduleMessage extends RailSimMessage<List<RunningService>>   {

    // For Jackson
    public ScheduleMessage() {
        super();
    }

    public ScheduleMessage(UUID msgKey, List<RunningService> services) {
        super(msgKey, services);
    }

    @Override
    public String toString() {
        return messageBody == null ? "N/A" : asString(messageBody) ;
    }

    @Override
    public List<RunningService> getMessageBody() {
        return messageBody;
    }

    private String asString(List<RunningService> services) {
        StringBuilder sb = new StringBuilder();
        for(RunningService service : services) {
            sb.append(service.toString()).append("\n");
        }
        return sb.toString();

    }
}

