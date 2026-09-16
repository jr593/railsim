package uk.co.raphel.railsim.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.railsim.common.TrainServiceDto;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.railsim.common.enums.SectionStatus;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/
@Slf4j(topic = "RailSimMessage")
@Getter @Setter
public class RailSimMessage<E>  implements Serializable {

    private UUID msgKey;
    private MessageType messageType;
    E messageBody;
    String miscInfo; // For errors and such like

    public RailSimMessage() {
        // For Jackson
    }

    public RailSimMessage(UUID msgKey,MessageType messageType) {
        this.msgKey = msgKey;
        this.messageType = messageType;
     }



    public String toString() {
        return messageType.name() ;
    }

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch(Exception e) {
            log.error("Could not convert message to send", e);
            this.setMessageType(MessageType.ERROR);
        }
        return "";
    }

    public static RailSimMessage fromjson(String json) {
        try {
            return new ObjectMapper().readValue(json, RailSimMessage.class);
        } catch(Exception e) {
            log.error("Could not convert message from json (" + json+") ", e);
        }
        return null;
    }
}

