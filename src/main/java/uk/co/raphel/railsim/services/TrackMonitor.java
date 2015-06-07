package uk.co.raphel.railsim.services;/**
 * Created by johnr on 30/05/2015.
 */

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.dto.TrackDiagramEntry;

import java.io.*;

@Component
@Scope("prototype")
public class TrackMonitor implements Runnable, ResourceLoaderAware {

    String name;
    private ResourceLoader resourceLoader;

    private Logger log = LoggerFactory.getLogger(TrackMonitor.class);

    IMap<Integer, TrackDiagramEntry> trackDiagram;
    IMap<String, String> statusMap;

    public TrackMonitor(){
    }


    public void run() {

        System.out.println(name + " is running");

        // Join hazel cast
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        trackDiagram = hazelcastInstance.getMap("trackDiagram");
        statusMap = hazelcastInstance.getMap("statusMap");

        statusMap.put("TRACKLOADED", "FALSE");


        // Start by loading the track sections maps
        loadTrackMap(getResource("classpath:TrackMapDown.csv"));
        loadTrackMap(getResource("classpath:TrackMapUp.csv"));

        log.info("Track Diagrams loaded " + trackDiagram.size());
        statusMap.put("TRACKLOADED", "TRUE");
    }

    private void loadTrackMap(Resource trackMap) {
        log.info("Loading resource");
        try{
            //InputStream is = trackMap.getInputStream();
            File inFile = trackMap.getFile()   ;
            BufferedReader br = new BufferedReader(new FileReader(inFile));

            String line;
            while ((line = br.readLine()) != null) {
                TrackDiagramEntry diagramEntry = new TrackDiagramEntry(line);
                trackDiagram.put(diagramEntry.getId(), diagramEntry);

            }
            br.close();


        }catch(IOException e){
            e.printStackTrace();
            log.error("Error loading track diagram", e);
        }

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setResourceLoader(ResourceLoader resourceLoader){
        this.resourceLoader = resourceLoader;
}

    public Resource getResource(String location){
        return resourceLoader.getResource(location);
    }
}
