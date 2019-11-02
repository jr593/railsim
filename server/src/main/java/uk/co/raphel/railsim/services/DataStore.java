package uk.co.raphel.railsim.services;

import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.TrackDiagramEntry;
import uk.co.raphel.railsim.common.TrainService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * * Created : 26/07/2015
 * * Author  : johnr
 **/
@Component
public class DataStore {

    private Map<String,String> statusMap;

    //
    // This map has the occupation schedule loaded into it.
    // Each Entry is : (int)Section No -> ((String)scheduled time, (String)Destination)
    // Destination boards can then use it to determine displayed services.
    private Map<Integer,Map<String,String>> trackOccupationSchedule = new HashMap<>();

    private List<TrainService> services;
    private Map<Integer, TrackDiagramEntry> trackDiagram;
    private int simClock;

    private boolean tracklock = false;

    Map<Integer,Map<String,String>> getTrackOccupationSchedule() {
        return trackOccupationSchedule;
    }
    void addOccupations(String destination, Map<Integer, String> occMap) {
        occMap.entrySet().forEach(e -> {
            if(!trackOccupationSchedule.containsKey(e.getKey())) {
                trackOccupationSchedule.put(e.getKey(), new HashMap<>());
            }
            trackOccupationSchedule.get(e.getKey()).put(e.getValue(), destination);
        });
    }
    String getSectionName(int sectionNumber) {
        return trackDiagram.get(sectionNumber).getName() + " (" + sectionNumber + ")";
    }
    void addService(TrainService service) {
        if(services == null) {
            services = new ArrayList<>();
        }
        services.add(service);

    }

    List<TrainService> getServices() {
        if(services == null) {
            services = new ArrayList<>();
        }
        return services;
    }

    void setSimClock(int simClock) {
        this.simClock = simClock;
    }

    int getSimClock() {
        return simClock;
    }

    void addTrackDiagramEntry(TrackDiagramEntry diagramEntry) {
        if(trackDiagram == null) {
            trackDiagram = new HashMap<>();
        }

        trackDiagram.put(diagramEntry.getId(), diagramEntry);
    }

    void releaseTrackLock() {
        tracklock = false;
    }

    boolean getTrackLock() {
        if(tracklock) {
            return false;
        } else {
            tracklock = true;
            return true;
        }
    }

    int calcTimeInSection(int sectionId, int speed) {

        TrackDiagramEntry trackSection = trackDiagram.get(sectionId);

        if(speed == 0) {
            return (int) (trackSection.getLength() / (double) trackSection.getSpeedLimit()) * 60;
        } else {
            return (int) (trackSection.getLength() / (double) speed) * 60;
        }

    }

    boolean isSectionFree(int nextSection) throws NullPointerException {

        return (trackDiagram.get(nextSection).getOccupiedBy().isEmpty())
                || trackDiagram.get(nextSection).isAllowMultipleOccupancy();
    }

    void clearTrackSection(int occupiedSection, Integer serviceId) {
        trackDiagram.get(occupiedSection).clearOccupiedBy(serviceId);
    }

    void occupySection(int eventSection, Integer id) {
        trackDiagram.get(eventSection).setOccupiedBy(id);
    }
}
