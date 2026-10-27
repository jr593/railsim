package uk.co.raphel.railsim.common.nats;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.broadcast.api.BroadcastMessageType;

import java.io.Serializable;
import java.util.UUID;

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/
@Slf4j(topic = "RailSimMessage")
@Getter @Setter
@BroadcastMessageType( "railsim.generic")
public abstract class RailSimMessage<T>  implements Serializable {

    protected UUID msgKey;
    protected T messageBody;

    protected RailSimMessage() {

    }

    protected RailSimMessage(UUID msgKey, T messageBody) {
       this.msgKey = msgKey;
       this.messageBody = messageBody;
    }

    public abstract String toString() ;

    public abstract Object getMessageBody();
}

