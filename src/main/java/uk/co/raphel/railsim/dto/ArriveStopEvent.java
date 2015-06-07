package uk.co.raphel.railsim.dto;/**
 * Created by johnr on 07/06/2015.
 */

/**
 * * Created : 07/06/2015
 * * Author  : johnr
 **/
public class ArriveStopEvent extends ServiceEvent {

    private int departureTime;

    public ArriveStopEvent(int eventSection, int timeOfDay, int departureTime) {
        super(eventSection, timeOfDay);
        this.departureTime = departureTime;
    }
}
