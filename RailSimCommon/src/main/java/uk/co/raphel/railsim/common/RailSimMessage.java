package uk.co.raphel.railsim.common;
/**
 * Created by johnr on 19/02/2017.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/
public class RailSimMessage implements Serializable {

    private MessageType messageType;
    private String clockTime;
    private String serviceName;
    private String serviceClass;
    private String serviceEngine;
    private Integer serviceId;

    private Integer sectionId;
    private String sectionName;
    private SectionStatus sectionStatus;

    private String miscInfo;
    private Logger log = LoggerFactory.getLogger(RailSimMessage.class);

    public RailSimMessage() {
        // For Jackson
    }

    public RailSimMessage(MessageType messageType, String clockTime,Integer serviceId, String serviceName,
                          String serviceEngine, String serviceClass,
                           Integer sectionId, String sectionName,
                          SectionStatus sectionStatus, String miscInfo) {
        this.messageType = messageType;
        this.clockTime = clockTime;
        this.serviceName = serviceName;
        this.serviceClass = serviceClass;
        this.serviceEngine = serviceEngine;
        this.serviceId = serviceId;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.sectionStatus = sectionStatus;
        this.miscInfo = miscInfo;
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
            return  new ObjectMapper().readValue(json, RailSimMessage.class);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getClockTime() {
        return clockTime;
    }

    public void setClockTime(String clockTime) {
        this.clockTime = clockTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getServiceEngine() {
        return serviceEngine;
    }

    public void setServiceEngine(String serviceEngine) {
        this.serviceEngine = serviceEngine;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
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
}
