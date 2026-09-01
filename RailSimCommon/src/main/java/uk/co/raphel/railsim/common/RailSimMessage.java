package uk.co.raphel.railsim.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.co.raphel.railsim.common.enums.MessageType;
import uk.co.raphel.railsim.common.enums.SectionStatus;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/
@Slf4j(topic = "RailSimMessage")
@Getter @Setter
public class RailSimMessage implements Serializable {

    private UUID msgKey;
    private MessageType messageType;
    private String clockTime;
    private TrainServiceDto trainServiceDto;

    private Integer sectionId;
    private String sectionName;
    private SectionStatus sectionStatus;

    private String miscInfo;

    private Map<Integer, Map<String, String>> occSched;

    public RailSimMessage() {
        // For Jackson
    }

    public RailSimMessage(UUID msgKey,MessageType messageType,String clockTime, Map<Integer, Map<String, String>> occSched) {
        this.msgKey = msgKey;
        this.messageType = messageType;
        this.clockTime = clockTime;
        this.occSched = occSched;
    }

    public RailSimMessage(UUID msgKey,MessageType messageType, String clockTime, TrainServiceDto trainServiceDto,
                          String sectionName,
                          SectionStatus sectionStatus, String miscInfo) {
        this.msgKey = msgKey;
        this.messageType = messageType;
        this.clockTime = clockTime;
        this.trainServiceDto = trainServiceDto;
        this.sectionId = trainServiceDto.getOccupiedSection();
        this.sectionName = sectionName;
        this.sectionStatus = sectionStatus;
        this.miscInfo = miscInfo;
    }

    public String toString() {
        return messageType + " " + trainServiceDto.getId()+ " " + sectionName + " " + sectionStatus + " " + miscInfo;
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

