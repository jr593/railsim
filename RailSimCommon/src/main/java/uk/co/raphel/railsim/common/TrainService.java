package uk.co.raphel.railsim.common;/**
 * Created by johnr on 30/05/2015.
 */

import uk.co.raphel.railsim.common.dto.*;

import java.util.ArrayList;
import java.util.List;

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
public class TrainService {

    private Integer startTime;
    private String serviceName;
    private String origin;
    private String destination;
    private String serviceClass;
    private String engine;

    private Integer id;

    private List<ServiceEvent> serviceEventList;

    private int currentServiceEvent;

    private boolean started = false;

    public TrainService() { // for Json
    }

    public TrainService(int startTime, String serviceName, String origin, String destination, String serviceClass,
                        String engine, int id, List<ServiceEvent> serviceEventList) {
        this.startTime = startTime;
        this.serviceName = serviceName;
        this.origin = origin;
        this.destination = destination;
        this.serviceClass = serviceClass;
        this.engine = engine;
        this.id = id;
        this.serviceEventList = serviceEventList;

        this.currentServiceEvent = 0;
    }
    public TrainService(String csvLine, List<Integer> indexList, int id ) {
        // e.g
        // Train,From,Class,Engine,Destination,1,2,3,4,5,6,7,8,9,10,11,200,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,201,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81
       //  00:25,Victoria,Pass,EMU,,S00.25,,,,S00.31,,S00.35,,,S00.38,S00.40,,S00.44,S00.46,,,T00.48,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
        String csv[] = csvLine.split(",");
        System.out.println(csvLine);
        startTime = timeToInt(csv[0]);
        serviceName = csv[0] + " " + csv[1] + " to " + csv[4];
        origin = csv[1];
        destination = csv[4];
        serviceClass = csv[2];
        engine = csv[3];

        serviceEventList = new ArrayList<>();

        for(int i = 5; i< csv.length; i++) {
            if(csv[i].length() >0) {
                serviceEventList.add(generateServiceEvent(indexList.get(i - 5), csv[i]));
            }
        }
        // Make sure last event is a terminator
        ServiceEvent lastEvent = serviceEventList.get(serviceEventList.size()-1);
        if(!(lastEvent.getServiceEventType() == ServiceEventType.TERMINATING)) {
            lastEvent = new ServiceEvent(lastEvent.getServiceEventType(),
                                         lastEvent.getEventSection(), lastEvent.getTimeOfDay());
            serviceEventList.set(serviceEventList.size()-1, lastEvent);
        }
        this.id = id;

        currentServiceEvent = 0;
    }

    public ServiceEvent getServiceEvent() {
        return serviceEventList.get(currentServiceEvent);
    }
    public void step() {
        if(currentServiceEvent < serviceEventList.size() -1) {
            currentServiceEvent++;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ServiceEvent getNextEvent() {
           try {
               return serviceEventList.get(currentServiceEvent+1);
           } catch(Exception e) {
               return null;
           }
    }
    public int getOccupiedSection() {
        return serviceEventList.get(currentServiceEvent).getEventSection();
    }

    public boolean isLastEvent() {
        return serviceEventList == null || serviceEventList.isEmpty() ||
                currentServiceEvent == serviceEventList.size()-1;
    }

    private int timeToInt(String hhmm) {
        if(hhmm.length() == 5) {
            return Integer.parseInt(hhmm.substring(0,2))*60 +
                    Integer.parseInt(hhmm.substring(3));
        }
        return 9999;
    }

    private ServiceEvent generateServiceEvent(Integer trackSection, String eventBase) {
        // Event base is from csv and can be :
        // Sxx.xx - Stopping service
        // Sxx.xx/yy.yy - Arrival and start time
        // Pxx.xx - Passing time
        // Txx.xx - Termination time

        if(eventBase.startsWith("T")) {
             return new ServiceEvent(ServiceEventType.TERMINATING, trackSection, timeToInt(eventBase.substring(1)));
        }
        if(eventBase.startsWith("P")) {
            try {
                return new ServiceEvent(ServiceEventType.PASSING,trackSection, timeToInt(eventBase.substring(1)));
            } catch(Exception e) {
                return new ServiceEvent(ServiceEventType.PASSING,trackSection, 0); // Blank passing event
            }
        }
        if(eventBase.startsWith("S")) {
            if(eventBase.contains("/")) {
                return new ServiceEvent(ServiceEventType.ARRIVESTOP, trackSection,
                                         timeToInt(eventBase.substring(1,6)),
                                         timeToInt(eventBase.substring(7)));
            } else {
                return new ServiceEvent(ServiceEventType.STOPPING, trackSection,
                        timeToInt(eventBase.substring(1)),
                        timeToInt(eventBase.substring(1)));
            }
        }

        return null;
    }

    public String toString() {
        return serviceName + " current evt=" + currentServiceEvent + " (" +
                serviceEventList.get(currentServiceEvent) + ")";
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public List<ServiceEvent> getServiceEventList() {
        return serviceEventList;
    }

    public void setServiceEventList(List<ServiceEvent> serviceEventList) {
        this.serviceEventList = serviceEventList;
    }

    public int getCurrentServiceEvent() {
        return currentServiceEvent;
    }

    public void setCurrentServiceEvent(int currentServiceEvent) {
        this.currentServiceEvent = currentServiceEvent;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
