package uk.co.raphel.railsim.dto;/**
 * Created by johnr on 07/06/2015.
 */

/**
 * * Created : 07/06/2015
 * * Author  : johnr
 **/
public class StoppingEvent extends ServiceEvent {

    public StoppingEvent(int eventSection, int timeOfDay) {
        super(eventSection, timeOfDay);
    }
}
