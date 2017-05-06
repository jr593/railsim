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
public class DestinationBoard extends JPanel  {

    String stationName;
    Integer stopNumber; // TODO Expand

    JTextField txtLocation = new JTextField();

    public DestinationBoard() {
        this.add(txtLocation);
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        GroupLayout jPanel2Layout = new javax.swing.GroupLayout(this);
        setLayout(jPanel2Layout);

    }
    public void onTrackEvent(RailSimMessage railSimMessage) {

    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
        txtLocation.setText(stationName + "(" + stopNumber + ")");
        System.out.println(stationName);
    }

    public Integer getStopNumber() {
        return stopNumber;
    }

    public void setStopNumber(Integer stopNumber) {
        this.stopNumber = stopNumber;
        txtLocation.setText(stationName + "(" + stopNumber + ")");
    }
}
