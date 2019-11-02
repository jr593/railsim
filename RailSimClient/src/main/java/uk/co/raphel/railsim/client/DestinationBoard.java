package uk.co.raphel.railsim.client;

import uk.co.raphel.railsim.common.RailSimMessage;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * * Created : 01/05/2017
 * * Author  : johnr
 **/
public class DestinationBoard extends JPanel implements RailSimMessageListener {

    private String stationName;
    private Integer stopNumber; // TODO Expand
    private JLabel stopName; // E.G. 'Up Main'
    private JLabel lbl1St;
    private JLabel lbl2Nd;
    private JLabel lblArr;
    private List<String> schedList = new ArrayList<>();

    DestinationBoard() {
        GridBagConstraints gridBagConstraints;

        lblArr = new JLabel();
        lbl1St = new JLabel();
        lbl2Nd = new JLabel();
        stopName = new JLabel();
        JLabel lbl_ARRTXT = new JLabel();
        JLabel lbl_1stTxt = new JLabel();
        JLabel lbl_2ndTxt = new JLabel();
        JLabel lbl_PLAT = new JLabel();

        setBackground(new java.awt.Color(51, 51, 51));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        setMaximumSize(new java.awt.Dimension(213, 85));
        setMinimumSize(new java.awt.Dimension(213, 85));
        setName(""); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        stopName.setFont(new java.awt.Font("Courier New", Font.BOLD, 12)); // NOI18N
        stopName.setForeground(new java.awt.Color(255, 153, 0));
        stopName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        stopName.setText(" <STOP NAME>  ");
        stopName.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        stopName.setMaximumSize(new java.awt.Dimension(300, 14));
        stopName.setMinimumSize(new java.awt.Dimension(300, 14));
        stopName.setPreferredSize(new java.awt.Dimension(300, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(stopName, gridBagConstraints);

        lblArr.setFont(new java.awt.Font("Courier New", Font.BOLD, 12)); // NOI18N
        lblArr.setForeground(new java.awt.Color(255, 153, 0));
        lblArr.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblArr.setText("              ");
        lblArr.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblArr.setMaximumSize(new java.awt.Dimension(300, 14));
        lblArr.setMinimumSize(new java.awt.Dimension(300, 14));
        lblArr.setPreferredSize(new java.awt.Dimension(300, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        add(lblArr, gridBagConstraints);

        lbl1St.setFont(new java.awt.Font("Courier New", Font.BOLD, 12)); // NOI18N
        lbl1St.setForeground(new java.awt.Color(255, 153, 0));
        lbl1St.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbl1St.setText("              ");
        lbl1St.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lbl1St.setMaximumSize(new java.awt.Dimension(300, 14));
        lbl1St.setMinimumSize(new java.awt.Dimension(300, 14));
        lbl1St.setPreferredSize(new java.awt.Dimension(300, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        add(lbl1St, gridBagConstraints);

        lbl2Nd.setFont(new java.awt.Font("Courier New", Font.BOLD, 12)); // NOI18N
        lbl2Nd.setForeground(new java.awt.Color(255, 153, 0));
        lbl2Nd.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbl2Nd.setText("              ");
        lbl2Nd.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lbl2Nd.setMaximumSize(new java.awt.Dimension(300, 14));
        lbl2Nd.setMinimumSize(new java.awt.Dimension(300, 14));
        lbl2Nd.setPreferredSize(new java.awt.Dimension(66, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(lbl2Nd, gridBagConstraints);


        lbl_PLAT.setFont(new java.awt.Font("Courier New", Font.BOLD, 12)); // NOI18N
        lbl_PLAT.setForeground(new java.awt.Color(255, 153, 0));
        lbl_PLAT.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbl_PLAT.setText("    ");
        lbl_PLAT.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lbl_PLAT.setMaximumSize(new java.awt.Dimension(50, 14));
        lbl_PLAT.setMinimumSize(new java.awt.Dimension(50, 14));
        lbl_PLAT.setPreferredSize(new java.awt.Dimension(50, 14));
        lbl_PLAT.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(lbl_PLAT, gridBagConstraints);

        lbl_ARRTXT.setFont(new java.awt.Font("Courier New", Font.BOLD, 12)); // NOI18N
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
        gridBagConstraints.gridy = 1;
        add(lbl_ARRTXT, gridBagConstraints);

        lbl_1stTxt.setFont(new java.awt.Font("Courier New", Font.BOLD, 12)); // NOI18N
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
        gridBagConstraints.gridy = 2;
        add(lbl_1stTxt, gridBagConstraints);

        lbl_2ndTxt.setFont(new java.awt.Font("Courier New", Font.BOLD, 12)); // NOI18N
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
        gridBagConstraints.gridy = 3;
        add(lbl_2ndTxt, gridBagConstraints);
    }

    @Override
    public void consume(RailSimMessage railSimMessage) {
        if (railSimMessage.getSectionId().intValue() == stopNumber) {
            String[] nextSrv = getNextServices(railSimMessage.getClockTime());
            switch (railSimMessage.getSectionStatus()) {
                case CLEARED :lblArr.setText("");   break;
                case OCCUPIED :
                    lblArr.setText(railSimMessage.getTrainService().getDestination());
                    lbl1St.setText(railSimMessage.getClockTime());
                break;
                case HOLDING : lbl1St.setText(railSimMessage.getClockTime());    break;
            }
            lbl1St.setText(nextSrv[0]);
            lbl2Nd.setText(nextSrv[1]);
        }
    }

    private String[] getNextServices(String clockTime) {

        String[] retval = {"", ""};
        int i = 0;
        boolean found = false;
        while (i < schedList.size() && !found) {
            if (!"".equals(schedList.get(i))) {
                if (timeIsAfter(schedList.get(i).substring(0, 5), clockTime)) {
                    retval[0] = schedList.get(i);
                    retval[1] = i < (schedList.size() - 1) ? schedList.get(i + 1) : "";
                    found = true;
                }
            }
            i++;
        }
        return retval;
    }

    private boolean timeIsAfter(String time1, String time2) {
        if (time1.length() == 5 && time2.length() == 5) {
            return Integer.parseInt(time1.substring(0, 2)) * 60 +
                    Integer.parseInt(time1.substring(3, 5)) >
                    Integer.parseInt(time2.substring(0, 2)) * 60 +
                            Integer.parseInt(time2.substring(3, 5));
        }
        return false;
    }

    @Override
    public void recvSchedule(RailSimMessage railSimMessage) {
        if (railSimMessage.getOccSched() != null) {
            if (railSimMessage.getOccSched().containsKey(stopNumber)) {
                schedList.addAll(railSimMessage.getOccSched().get(stopNumber).entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.toList()));
                Collections.sort(schedList);
            }
            String[] nextSrv = getNextServices(railSimMessage.getClockTime());
            lbl1St.setText(nextSrv[0]);
            lbl2Nd.setText(nextSrv[1]);

        }
    }

    public String getStationName() {
        return stationName;
    }

    void setStationName(String stationName) {
        this.stationName = stationName;
        lblArr.setText(stationName + "(" + stopNumber + ")");
    }

    public Integer getStopNumber() {
        return stopNumber;
    }

    void setStopNumber(Integer stopNumber) {
        this.stopNumber = stopNumber;
        lblArr.setText(stationName + "(" + stopNumber + ")");
    }
}
