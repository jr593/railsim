package uk.co.raphel.railsim.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/
public class RailSimMessage implements Serializable {

    private MessageType messageType;
    private String clockTime;
    private TrainService trainService;

    private Integer sectionId;
    private String sectionName;
    private SectionStatus sectionStatus;

    private String miscInfo;

    private Map<Integer, Map<String, String>> occSched;
    private final Logger log = LoggerFactory.getLogger(RailSimMessage.class);

    public RailSimMessage() {
        // For Jackson
    }

    public RailSimMessage(MessageType messageType,String clockTime, Map<Integer, Map<String, String>> occSched) {
        this.messageType = messageType;
        this.clockTime = clockTime;
        this.occSched = occSched;
    }

    public RailSimMessage(MessageType messageType, String clockTime, TrainService trainService,
                          String sectionName,
                          SectionStatus sectionStatus, String miscInfo) {
        this.messageType = messageType;
        this.clockTime = clockTime;
        this.trainService = trainService;
        this.sectionId = trainService.getOccupiedSection();
        this.sectionName = sectionName;
        this.sectionStatus = sectionStatus;
        this.miscInfo = miscInfo;
    }

    public String toString() {
        return messageType + " " + trainService.getServiceName() + " " + sectionName + " " + sectionStatus + " " + miscInfo;
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
            e.printStackTrace();
        }
        return null;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    private void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getClockTime() {
        return clockTime;
    }

    public void setClockTime(String clockTime) {
        this.clockTime = clockTime;
    }

    public TrainService getTrainService() {
        return trainService;
    }

    public void setTrainService(TrainService trainService) {
        this.trainService = trainService;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public SectionStatus getSectionStatus() {
        return sectionStatus;
    }

    public void setSectionStatus(SectionStatus sectionStatus) {
        this.sectionStatus = sectionStatus;
    }

    public String getMiscInfo() {
        return miscInfo;
    }

    public void setMiscInfo(String miscInfo) {
        this.miscInfo = miscInfo;
    }

    public Map<Integer, Map<String, String>> getOccSched() {
        return occSched;
    }

    public void setOccSched(Map<Integer, Map<String, String>> occSched) {
        this.occSched = occSched;
    }
}

