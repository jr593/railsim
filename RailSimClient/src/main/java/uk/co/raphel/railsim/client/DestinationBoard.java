package uk.co.raphel.railsim.client;/**
 * Created by johnr on 01/05/2017.
 */

import uk.co.raphel.railsim.common.RailSimMessage;
import uk.co.raphel.railsim.common.SectionStatus;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * * Created : 01/05/2017
 * * Author  : johnr
 **/
public class DestinationBoard extends JPanel implements RailSimMessageListener {

    String stationName;
    Integer stopNumber; // TODO Expand
    private javax.swing.JLabel lbl_1st;
    private javax.swing.JLabel lbl_1stTxt;
    private javax.swing.JLabel lbl_2nd;
    private javax.swing.JLabel lbl_2ndTxt;
    private javax.swing.JLabel lbl_ARRTXT;
    private javax.swing.JLabel lbl_Arr;
    //JLabel txtLocation = new JLabel();
    List<String> schedList = new ArrayList<>();

    public DestinationBoard() {
        java.awt.GridBagConstraints gridBagConstraints;

        lbl_Arr = new javax.swing.JLabel();
        lbl_1st = new javax.swing.JLabel();
        lbl_2nd = new javax.swing.JLabel();
        lbl_ARRTXT = new javax.swing.JLabel();
        lbl_1stTxt = new javax.swing.JLabel();
        lbl_2ndTxt = new javax.swing.JLabel();

        setBackground(new java.awt.Color(51, 51, 51));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0)));
        setMaximumSize(new java.awt.Dimension(213, 55));
        setMinimumSize(new java.awt.Dimension(213, 55));
        setName(""); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        lbl_Arr.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        lbl_Arr.setForeground(new java.awt.Color(255, 153, 0));
        lbl_Arr.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbl_Arr.setText("00:00 <NotSET>");
        lbl_Arr.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lbl_Arr.setMaximumSize(new java.awt.Dimension(300, 14));
        lbl_Arr.setMinimumSize(new java.awt.Dimension(300, 14));
        lbl_Arr.setPreferredSize(new java.awt.Dimension(300, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(lbl_Arr, gridBagConstraints);

        lbl_1st.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        lbl_1st.setForeground(new java.awt.Color(255, 153, 0));
        lbl_1st.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbl_1st.setText("00:00 <NotSET>");
        lbl_1st.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lbl_1st.setMaximumSize(new java.awt.Dimension(300, 14));
        lbl_1st.setMinimumSize(new java.awt.Dimension(300, 14));
        lbl_1st.setPreferredSize(new java.awt.Dimension(300, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        add(lbl_1st, gridBagConstraints);

        lbl_2nd.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        lbl_2nd.setForeground(new java.awt.Color(255, 153, 0));
        lbl_2nd.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbl_2nd.setText("00:00 <NotSET>");
        lbl_2nd.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lbl_2nd.setMaximumSize(new java.awt.Dimension(300, 14));
        lbl_2nd.setMinimumSize(new java.awt.Dimension(300, 14));
        lbl_2nd.setPreferredSize(new java.awt.Dimension(66, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(lbl_2nd, gridBagConstraints);

        lbl_ARRTXT.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        lbl_ARRTXT.setForeground(new java.awt.Color(255, 153, 0));
        lbl_ARRTXT.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl_ARRTXT.setText("ARR:");
        lbl_ARRTXT.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lbl_ARRTXT.setMaximumSize(new java.awt.Dimension(50, 14));
        lbl_ARRTXT.setMinimumSize(new java.awt.Dimension(50, 14));
        lbl_ARRTXT.setPreferredSize(new java.awt.Dimension(50, 14));
        lbl_ARRTXT.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(lbl_ARRTXT, gridBagConstraints);

        lbl_1stTxt.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        lbl_1stTxt.setForeground(new java.awt.Color(255, 153, 0));
        lbl_1stTxt.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl_1stTxt.setText("1st:");
        lbl_1stTxt.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lbl_1stTxt.setMaximumSize(new java.awt.Dimension(50, 14));
        lbl_1stTxt.setMinimumSize(new java.awt.Dimension(50, 14));
        lbl_1stTxt.setPreferredSize(new java.awt.Dimension(50, 14));
        lbl_1stTxt.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(lbl_1stTxt, gridBagConstraints);

        lbl_2ndTxt.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        lbl_2ndTxt.setForeground(new java.awt.Color(255, 153, 0));
        lbl_2ndTxt.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl_2ndTxt.setText("2nd:");
        lbl_2ndTxt.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lbl_2ndTxt.setMaximumSize(new java.awt.Dimension(50, 14));
        lbl_2ndTxt.setMinimumSize(new java.awt.Dimension(50, 14));
        lbl_2ndTxt.setPreferredSize(new java.awt.Dimension(50, 14));
        lbl_2ndTxt.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        add(lbl_2ndTxt, gridBagConstraints);
    }

    @Override
    public void consume(RailSimMessage railSimMessage) {
        if(railSimMessage.getSectionId().intValue() == stopNumber) {
            String[] nextSrv = getNextServices(railSimMessage.getClockTime());
            switch(railSimMessage.getSectionStatus()) {
               case CLEARED:
                    lbl_Arr.setText("");
                    break;
                case OCCUPIED:
                    lbl_Arr.setText(railSimMessage.getTrainService().getDestination());
                    lbl_1st.setText(railSimMessage.getClockTime());
                    break;
                case HOLDING:
                    lbl_1st.setText(railSimMessage.getClockTime());

                    break;
            }
            lbl_1st.setText(nextSrv[0]);
            lbl_2nd.setText(nextSrv[1]);
        }
    }

    private String[] getNextServices(String clockTime) {

        String[] retval = {"",""};
        int i =0;
        boolean found = false;
        while(i<schedList.size() && !found) {
            if(!"".equals(schedList.get(i))) {
                if(timeIsAfter(schedList.get(i).substring(0,5),clockTime)) {
                    retval[0] = schedList.get(i);
                    retval[1] = i < (schedList.size()-1) ? schedList.get(i+1) : "";
                    found = true;
                }
            }
            i++;
        }
        return retval;
    }

    private boolean timeIsAfter(String time1, String time2) {
        if(time1.length() == 5 && time2.length()==5) {
            return Integer.parseInt(time1.substring(0,2))*60 +
                    Integer.parseInt(time1.substring(3,5))       >
                    Integer.parseInt(time2.substring(0,2))*60 +
                            Integer.parseInt(time2.substring(3,5));
        }
        return false;
    }
    @Override
    public void recvSchedule(RailSimMessage railSimMessage) {
        if(railSimMessage.getOccSched() != null) {
            if(railSimMessage.getOccSched().containsKey(stopNumber)) {
                for(Map.Entry<String, String> entry : railSimMessage.getOccSched().get(stopNumber).entrySet()) {
                    schedList.add(entry.getKey() + ":" + entry.getValue());
                }
                Collections.sort(schedList);
            }
            String[] nextSrv =  getNextServices(railSimMessage.getClockTime());
            lbl_1st.setText(nextSrv[0]);
            lbl_2nd.setText(nextSrv[1]);

        }
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
        lbl_Arr.setText(stationName + "(" + stopNumber + ")");
    }

    public Integer getStopNumber() {
        return stopNumber;
    }

    public void setStopNumber(Integer stopNumber) {
        this.stopNumber = stopNumber;
        lbl_Arr.setText(stationName + "(" + stopNumber + ")");
    }
}
