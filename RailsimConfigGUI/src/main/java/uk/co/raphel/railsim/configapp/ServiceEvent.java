package uk.co.raphel.railsim.configapp;/**
 * Created by johnr on 21/03/2017.
 */

/**
 * * Created : 21/03/2017
 * * Author  : johnr
 **/
public class ServiceEvent {

    private int trackId;
    private String evtText;

    public ServiceEvent(int trackId, String evtText) {
        this.trackId = trackId;
        this.evtText = evtText;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public String getEvtText() {
        return evtText;
    }

    public void setEvtText(String evtText) {
        this.evtText = evtText;
    }
}
