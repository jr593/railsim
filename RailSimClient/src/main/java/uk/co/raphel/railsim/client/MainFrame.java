package uk.co.raphel.railsim.client;/**
 * Created by johnr on 19/02/2017.
 */

/**
 * * Created : 19/02/2017
 * * Author  : johnr
 **/

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import sun.security.krb5.internal.crypto.Des;
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
public class MainFrame extends JFrame implements ResourceLoaderAware, RailListener {


    //JScrollPane scrollPane = new JScrollPane();
    Map<String,List<DestinationBoard>> destinations = new HashMap<>();
    private ResourceLoader resourceLoader;


    public void init() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(new Dimension(600, 400));

        destinations = loadDestinations();

       // scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        //JPanel boardPanel = new JPanel();
        GridBagLayout gridBag = new GridBagLayout();
        //boardPanel.setLayout(gridBag);
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridBag.setConstraints(this,c);
        //scrollPane.add(boardPanel);

        //add(scrollPane);

        int gridRow = 0;
        for(Map.Entry<String,List<DestinationBoard>> entry  : destinations.entrySet()) {
            int gridCol =0;
            for(DestinationBoard destinationBoard : entry.getValue()) {
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                GridBagConstraints g = new GridBagConstraints();
                g.gridx = gridRow;
                g.gridy = gridCol++;
                add(destinationBoard);
            }
            gridRow++;
        }

        setVisible(true);
        setState(Frame.NORMAL);
    }

    @Override
    public void listen(String message) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        RailSimMessage railSimMessage = RailSimMessage.fromjson(message);

        if(railSimMessage != null ) {
            for(Map.Entry<String,List<DestinationBoard>> dest : destinations.entrySet()) {
                for(DestinationBoard brd :  dest.getValue()) {
                    brd.onTrackEvent(railSimMessage);
                }
            }
        }

    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private Map<String,List<DestinationBoard>> loadDestinations() {
        Resource resource = getResource("classpath:destinations.csv");
        Map<String,List<DestinationBoard>> result = new LinkedHashMap<>();

        try{
            //InputStream is = resource.getInputStream();
            File inFile = resource.getFile()   ;

            BufferedReader br = new BufferedReader(new FileReader(inFile));

            String line;
            // Read the header line
            String headerLine = br.readLine();

            while ((line = br.readLine()) != null) {
                 String[] destLine = line.split(",");
                if(destLine.length > 3) {
                    List<DestinationBoard> boards = new ArrayList<>();
                    for(int i=2; i<destLine.length; i++) {
                        DestinationBoard brd = new DestinationBoard();
                        brd.setStationName(destLine[1]);
                        brd.setStopNumber(Integer.parseInt(destLine[i]));
                        boards.add(brd);
                    }
                    result.put(destLine[1], boards);
                }


            }
            br.close();


        }catch(IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    private Resource getResource(String location){
        return resourceLoader.getResource(location);
    }
}