package uk.co.raphel.railsim.dto;/**
 * Created by johnr on 07/06/2015.
 */

/**
 * * Created : 07/06/2015
 * * Author  : johnr
 **/
public class TerminatingEvent extends ServiceEvent {

    public TerminatingEvent(int trackSection, int timeOfDay) {

        super(trackSection, timeOfDay);
    }

    @Override
    public boolean isStopping() {
        return false;
    }

    @Override
    public int getScheduledExitTime() {
        return super.getTimeOfDay();
    }
}
