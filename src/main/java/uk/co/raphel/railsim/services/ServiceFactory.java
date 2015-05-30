package uk.co.raphel.railsim.services;/**
 * Created by johnr on 30/05/2015.
 */

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.dto.TrackDiagramEntry;
import uk.co.raphel.railsim.dto.TrainService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

@Component
@Scope("prototype")
public class ServiceFactory implements Runnable, ResourceLoaderAware {

    String name;
    TaskExecutor taskExecutor;
    private Logger log = LoggerFactory.getLogger(ServiceFactory.class);
    private ResourceLoader resourceLoader;

    private Map<Integer, TrainService> services = new TreeMap<>();


    public ServiceFactory(){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void run() {

        System.out.println(name + " is running");
        // Join hazel cast
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        IMap<String, String> statusMap = hazelcastInstance.getMap("statusMap");
        try {
            while(statusMap == null || statusMap.get("TRACKLOADED") == null ||
                    !statusMap.get("TRACKLOADED").equalsIgnoreCase("TRUE") ) {

                log.warn("Waiting for track diagram to load");
                Thread.sleep(5000);
            }
        }    catch(InterruptedException ie) {
            // Do nothing
        }
        IMap<Integer, TrackDiagramEntry> trackDiagram = hazelcastInstance.getMap("trackDiagram");
        log.info("Track diagram accessable");
        log.info("Attempting to load services");
        // Start by loading the track sections map
        Resource resource =
                getResource("classpath:resources/services.csv");
        try{
            InputStream is = resource.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null) {
                TrainService service = new TrainService(line);
                services.put(service.getStartTime(), service);

            }
            br.close();

            log.info("All services loaded");

        }catch(IOException e){
            e.printStackTrace();
            log.error("Error loading services", e);
        }



        try {
            Thread.sleep(5000);
            log.info("Running loop");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(name + " is running");
    }

    public void setResourceLoader(ResourceLoader resourceLoader){
        this.resourceLoader = resourceLoader;
    }

    public Resource getResource(String location){
        return resourceLoader.getResource(location);
    }
}
