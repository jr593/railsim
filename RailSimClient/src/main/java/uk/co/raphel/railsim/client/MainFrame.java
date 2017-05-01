package uk.co.raphel.railsim.client;/**
 * Created by johnr on 19/02/2017.
 */

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;


public class MainFrame extends JFrame {

    JPanel destinationPanel = new JPanel();
    JScrollPane scrollPane = new JScrollPane(destinationPanel);
    Map<String,List<DestinationBoard>> destinations = new HashMap<>();


    public void init() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(new Dimension(600, 400));

        loadDestinations();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        //setLayout(gridbag);
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridbag.setConstraints(scrollPane, c);
        add(scrollPane);
        c.fill = GridBagConstraints.HORIZONTAL;
        scrollPane.setLayout(gridbag);

        int gridRow = 0;
        for(Map.Entry<String,List<DestinationBoard>> entry  : destinations.entrySet()) {
            int gridCol =0;
            for(DestinationBoard destinationBoard : entry.getValue()) {
                c.gridx = gridRow;
                c.gridy = gridCol;
                scrollPane.add(destinationBoard,c);
            }
        }

        setVisible(true);
        setState(Frame.NORMAL);
    }
}