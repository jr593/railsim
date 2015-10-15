package uk.co.raphel.railsim.dto;/**
 * Created by johnr on 07/06/2015.
 */

/**
 * * Created : 07/06/2015
 * * Author  : johnr
 **/
public abstract class ServiceEvent {

    private int eventSection;
    private int timeOfDay;


    public ServiceEvent(int eventSection, int timeOfDay) {
        this.eventSection = eventSection;
        this.timeOfDay = timeOfDay;
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

    public abstract boolean isStopping();

    public abstract int getScheduledExitTime();

}
