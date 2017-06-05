package uk.co.raphel.railsim.client;/**
 * Created by johnr on 19/02/2017.
 */

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import sun.security.krb5.internal.crypto.Des;
import uk.co.raphel.railsim.common.MessageType;
import uk.co.raphel.railsim.common.RailSimMessage;


import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;


@org.springframework.stereotype.Component
public class MainFrame extends JFrame implements ResourceLoaderAware {

    @Autowired
    private ApplicationContext appContext;

    MessageProcessor messageProcessor;

    List<Destination> destinations;

    private ResourceLoader resourceLoader;


    public void init() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(new Dimension(800, 800));

        messageProcessor = (MessageProcessor) appContext.getBean("railListener");

        destinations = loadDestinations();

        GridBagConstraints gridBagConstraints;

        JScrollPane scrolPane = new JScrollPane();
        JPanel backPane = new JPanel();

        scrolPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        backPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        backPane.setLayout(new java.awt.GridBagLayout());
        int currRow = 0;
        for(Destination entry : destinations) {
            int curCol = 0;
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = currRow++;
            backPane.add(new JLabel(entry.getName()),gridBagConstraints);
            for(DestinationBoard brd : entry.getDestinationBoards()) {
                brd.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = curCol;
                gridBagConstraints.gridy = currRow;
                backPane.add(brd, gridBagConstraints);
                brd.setPreferredSize(new Dimension(200,40));
                brd.setVisible(true);
                messageProcessor.addListener(brd);
                curCol++;
            }
            currRow++;
        }

        scrolPane.setViewportView(backPane);
        /*GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(scrolPane, GroupLayout.PREFERRED_SIZE, 778, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 10, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(scrolPane, GroupLayout.PREFERRED_SIZE, 612, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
        );*/

        add(scrolPane);

        setVisible(true);
        setState(Frame.NORMAL);
    }



    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private List<Destination> loadDestinations() {
        Resource resource = getResource("classpath:destinations.csv");
        List<Destination> result = new LinkedList<>();

        try {
            File inFile = resource.getFile();

            BufferedReader br = new BufferedReader(new FileReader(inFile));

            String line;
            // Read the header line
            String headerLine = br.readLine();

            while((line = br.readLine()) != null) {
                String[] destLine = line.split(",");
                if(destLine.length > 3) {
                    List<DestinationBoard> boards = new ArrayList<>();
                    Destination destination = new Destination();
                    destination.setName(destLine[1]);
                    destination.setId(Integer.parseInt(destLine[0]));
                    for(int i = 2; i < destLine.length; i++) {
                        DestinationBoard brd = new DestinationBoard();
                        brd.setStationName(destLine[1]);
                        brd.setStopNumber(Integer.parseInt(destLine[i]));
                        boards.add(brd);
                    }
                    destination.setDestinationBoards(boards);
                    result.add(destination);
                }


            }
            br.close();


        } catch(IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Resource getResource(String location) {
        return resourceLoader.getResource(location);
    }

}

