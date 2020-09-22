package uk.co.raphel.railsim.client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.MessageType;
import uk.co.raphel.railsim.common.RailSimMessage;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class MainFrame extends JFrame implements ResourceLoaderAware {


    @Autowired
    KafkaConsumer kafkaConsumer;

    private ResourceLoader resourceLoader;


    @PostConstruct
    public void init() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(new Dimension(800, 800));

        List<Destination> destinations = loadDestinations();

        GridBagConstraints gridBagConstraints;

        JScrollPane scrolPane = new JScrollPane();
        JPanel backPane = new JPanel();

        scrolPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        backPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        backPane.setLayout(new java.awt.GridBagLayout());
        int currRow = 0;
        for (Destination entry : destinations) {
            int curCol = 0;
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = currRow++;
            backPane.add(new JLabel(entry.getName()), gridBagConstraints);
            for (DestinationBoard brd : entry.getDestinationBoards()) {
                brd.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = curCol;
                gridBagConstraints.gridy = currRow;
                gridBagConstraints.insets = new Insets(2,2,2,2);
                backPane.add(brd, gridBagConstraints);
                brd.setPreferredSize(new Dimension(200, 70));
                brd.setVisible(true);
                kafkaConsumer.addListener(brd);
                curCol++;
            }
            currRow++;
        }

        scrolPane.setViewportView(backPane);

        add(scrolPane);

        setVisible(true);
        setState(Frame.NORMAL);
    }





    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private List<Destination> loadDestinations() {
        Resource resource = resourceLoader.getResource("classpath:destinations.csv");
        List<Destination> result = new LinkedList<>();

        try {
            File inFile = resource.getFile();

            BufferedReader br = new BufferedReader(new FileReader(inFile));

            String line;
            // Read the header line
            String headerLine = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] destLine = line.split(",");
                if (destLine.length > 3) {
                    List<DestinationBoard> boards = new ArrayList<>();
                    Destination destination = new Destination();
                    destination.setName(destLine[1]);
                    destination.setId(Integer.parseInt(destLine[0]));
                    for (int i = 2; i < destLine.length; i++) {
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


        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


}

