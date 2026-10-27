package uk.co.raphel.railsim.common.nats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import uk.co.raphel.broadcast.api.BroadcastMessageType;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter @Setter
@BroadcastMessageType( "railsim.stats")
public class StatsMessage extends RailSimMessage<LocalTime>   {


    @JsonIgnore
    DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

    // for jackson
    public StatsMessage() {
        super();
    }

    public StatsMessage(UUID msgKey, LocalTime simulatorTime) {
        super(msgKey, simulatorTime);
    }

    @Override
    public String toString() {
        return messageBody == null ? "hh:mm" : messageBody.format(df);
    }

    @Override
    public LocalTime getMessageBody() {
        return messageBody;
    }
}

