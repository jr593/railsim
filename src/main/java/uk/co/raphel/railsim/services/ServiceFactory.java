package uk.co.raphel.railsim.services;/**
 * Created by johnr on 30/05/2015.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.MessageType;
import uk.co.raphel.railsim.common.RailSimMessage;
import uk.co.raphel.railsim.common.TrackDiagramEntry;
import uk.co.raphel.railsim.common.TrainService;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@EnableScheduling
public class ServiceFactory implements Runnable, ResourceLoaderAware {

   // String name;
   // TaskExecutor taskExecutor;
    private final Logger log = LoggerFactory.getLogger(ServiceFactory.class);
    private ResourceLoader resourceLoader;

    private int simClock  = 0;       // Elapsed sim time in seconds

    private int simRate;

    @Autowired
    ThreadPoolTaskExecutor executor;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    DataStore ds;

    @PostConstruct
    public void postConstruct() {
       // log.info(name + " is running");

        // Start by loading the track sections maps
        loadTrackMap(getResource("classpath:TrackMapDown.csv"));
        loadTrackMap(getResource("classpath:TrackMapUp.csv"));

        // TODO Send track diagram to clients

        log.info("Track diagram accessable");
        log.info("Attempting to load services");

        Resource resource = getResource("classpath:ServicesDown1.csv");
        int trainId = 1;
        trainId = loadServiceSet(resource, trainId);
        resource = getResource("classpath:ServicesUp1.csv");
        trainId = loadServiceSet(resource,trainId);

        log.info("Services total = " + trainId);

        // Send initial track occupation schedule
        rabbitTemplate.convertAndSend(new RailSimMessage(MessageType.SCHEDULE, simTimeToClock(simClock),
                ds.getTrackOccupationSchedule()));


        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(100);
        executor.initialize();
    }

//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }

//    public TaskExecutor getTaskExecutor() {
//        return taskExecutor;
//    }

//    public void setTaskExecutor(TaskExecutor taskExecutor) {
//        this.taskExecutor = taskExecutor;
//    }

    @Scheduled(fixedDelay = 1000)
    public void run() {


        simClock += 1;          // Advance one minute per loop iteration
        ds.setSimClock(simClock);

        ds.getServices().stream().forEach(srv -> {
            if(!srv.getServiceEventList().isEmpty()) {
                if(srv.getServiceEventList().get(0).getTimeOfDay() == (simClock )  && !srv.isStarted())   {
                    System.out.println("Kick off : " + srv.getServiceName());
                    ServiceRunner runner = new ServiceRunner(srv, ds, rabbitTemplate);
                    executor.execute(runner);

                }
            }
        });
    }

    private int loadServiceSet(Resource resource,int firstId) {
        int retVal = firstId;
        try{
            InputStream is = resource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            // Read the header line and index the track sections
            String headerLine = br.readLine();
            List<Integer> indexList = new ArrayList<>();
            System.out.println(headerLine);
            if(headerLine != null && headerLine.length() >0) {
                String indexes[] = headerLine.split(",");
                for(int i= 5; i<indexes.length; i++) {
                    int trackSection = Integer.parseInt(indexes[i]);
                    indexList.add(trackSection);
                }
            }
            while ((line = br.readLine()) != null) {
                // Only read lines with service defined (may be being built still!)
                if(line.split(",").length >=5) {
                    TrainService service = new TrainService(line, indexList, retVal++);
                    ds.addService(service);
                    constructOccupationSchedule(line, indexList);
                }

            }
            br.close();

            log.info("" + ds.getServices().size() + " services loaded");

        }catch(IOException e){
            e.printStackTrace();
            log.error("Error loading services", e);
        }
        return retVal;
    }
    private void loadTrackMap(Resource trackMap) {
        log.info("Loading resource");
        try{
            //InputStream is = trackMap.getInputStream();
            File inFile = trackMap.getFile()   ;
            BufferedReader br = new BufferedReader(new FileReader(inFile));

            String line = br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                TrackDiagramEntry diagramEntry = new TrackDiagramEntry(line);
                System.out.println(diagramEntry.getId() + ":" + diagramEntry.getName());
                ds.addTrackDiagramEntry(diagramEntry);

            }
            br.close();


        }catch(IOException e){
            e.printStackTrace();
            log.error("Error loading track diagram", e);
        }

    }

    private void constructOccupationSchedule(String csvLine, List<Integer> indexList) {
        // e.g
        // Train,From,Class,Engine,Destination,1,2,3,4,5,6,7,8,9,10,11,200,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,201,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81
        //  00:25,Victoria,Pass,EMU,,S00.25,,,,S00.31,,S00.35,,,S00.38,S00.40,,S00.44,S00.46,,,T00.48,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
        String csv[] = csvLine.split(",");
        String destination = csv[4];

        Map<Integer,String> occSched = new HashMap<>();   // Schedule for this line   = section ->> time

        for(int i = 5; i< csv.length; i++) {
            if(csv[i].length() >0) {
                int sectionNumber = indexList.get(i-5);
                String event = csv[i];
                if(event.startsWith("S")) {
                    // Add stopping service to list
                    occSched.put(sectionNumber, event.substring(1));
                }
            }
        }
        if(!occSched.isEmpty()) {
            ds.addOccupations(destination, occSched);
        }

    }
    private String simTimeToClock(int simTime) {
        return " " + simTime/60 + ":" + simTime % 60;
    }


    public void setResourceLoader(ResourceLoader resourceLoader){
        this.resourceLoader = resourceLoader;
    }

    public Resource getResource(String location){
        return resourceLoader.getResource(location);
    }

    public int getSimClock() {
        return simClock;
    }

    public void setSimClock(int simClock) {
        this.simClock = simClock;
    }

    public int getSimRate() {
        return simRate;
    }

    public void setSimRate(int simRate) {
        this.simRate = simRate;
    }

}
