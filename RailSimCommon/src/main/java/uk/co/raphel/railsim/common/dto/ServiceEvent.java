package uk.co.raphel.railsim.common.dto;

import uk.co.raphel.railsim.common.ServiceEventType;

import static uk.co.raphel.railsim.common.ServiceEventType.ARRIVESTOP;

/**
 * * Created : 07/06/2015
 * * Author  : johnr
 **/
public class ServiceEvent {

    private ServiceEventType serviceEventType;
    private int eventSection;
    private int timeOfDay;
    private int departureTime;


    public ServiceEvent() {
    }

    public ServiceEvent(ServiceEventType serviceEventType, int eventSection, int timeOfDay) {
        this.serviceEventType = serviceEventType;
        this.eventSection = eventSection;
        this.timeOfDay = timeOfDay;
    }

    public ServiceEvent(ServiceEventType serviceEventType, int eventSection, int timeOfDay, int departure) {
        this(serviceEventType, eventSection, timeOfDay);
        this.departureTime = departure;
    }

    public int getEventSection() {
        return eventSection;
    }

    public void setEventSection(int eventSection) {
        this.eventSection = eventSection;
    }

    public int getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(int timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public ServiceEventType getServiceEventType() {
        return serviceEventType;
    }

    public void setServiceEventType(ServiceEventType serviceEventType) {
        this.serviceEventType = serviceEventType;
    }

    public int getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
    }

   /* public boolean isStopping() {
        switch (serviceEventType) {
            case ARRIVESTOP:
                return true;
            case PASSING:
                return false;
            case STOPPING:
                return true;
            case TERMINATING:
                return false;
            default:
                return false;
        }
    }*/

    public int getScheduledExitTime() {
        return serviceEventType == ARRIVESTOP ? departureTime : getTimeOfDay();
     }

    public void setScheduledExitTime(int scheduledExitTime) {
        if(serviceEventType == ARRIVESTOP ) {
            departureTime = scheduledExitTime;
        } else {
            timeOfDay = scheduledExitTime;
        }

    }

    public String toString() {
        if (serviceEventType == ARRIVESTOP) {
            return "ArriveStopEvent Section " + getEventSection() + " at " + getTimeOfDay() + "/" + departureTime;
        } else {
            return serviceEventType + " Section " + getEventSection() + " at " + getTimeOfDay();
        }
    }

}
