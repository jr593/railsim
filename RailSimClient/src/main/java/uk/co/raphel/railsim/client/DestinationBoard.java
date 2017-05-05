package uk.co.raphel.railsim.client;/**
 * Created by johnr on 01/05/2017.
 */

import uk.co.raphel.railsim.common.RailSimMessage;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * * Created : 01/05/2017
 * * Author  : johnr
 **/
public class DestinationBoard extends JPanel implements TrackEventListener {

    String stationName;
    Integer stopNumber; // TODO Expand

    @Override
    public void onTrackEvent() {

    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public Integer getStopNumber() {
        return stopNumber;
    }

    public void setStopNumber(Integer stopNumber) {
        this.stopNumber = stopNumber;
    }
}
