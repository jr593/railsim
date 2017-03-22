package uk.co.raphel.railsim.services;/**
 * Created by johnr on 26/07/2015.
 */

import org.springframework.stereotype.Component;
import uk.co.raphel.railsim.common.TrackDiagramEntry;
import uk.co.raphel.railsim.dto.TrainService;

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


    private List<TrainService> services;
    private Map<Integer, TrackDiagramEntry> trackDiagram;
    private int simClock;

    private boolean tracklock = false;

    public String getSectionName(int sectionNumber) {
        return trackDiagram.get(sectionNumber).getName() + " (" + sectionNumber + ")";
    }
    public void addService(TrainService service) {
        if(services == null) {
            services = new ArrayList<>();
        }
        services.add(service);

    }

    public List<TrainService> getServices() {
        if(services == null) {
            services = new ArrayList<>();
        }
        return services;
    }

    public void setSimClock(int simClock) {
        this.simClock = simClock;
    }

    public int getSimClock() {
        return simClock;
    }

    public void addTrackDiagramEntry(TrackDiagramEntry diagramEntry) {
        if(trackDiagram == null) {
            trackDiagram = new HashMap<>();
        }

        trackDiagram.put(diagramEntry.getId(), diagramEntry);
    }

    public void releaseTrackLock() {
        tracklock = false;
    }

    public boolean getTrackLock() {
        if(tracklock) {
            return false;
        } else {
            tracklock = true;
            return true;
        }
    }

    public int calcTimeInSection(int sectionId, int speed) {

        TrackDiagramEntry trackSection = trackDiagram.get(sectionId);

        if(speed == 0) {
            return (int) (trackSection.getLength() / (double) trackSection.getSpeedLimit()) * 60;
        } else {
            return (int) (trackSection.getLength() / (double) speed) * 60;
        }

    }

    public boolean isSectionFree(int nextSection) throws NullPointerException {

        return (trackDiagram.get(nextSection).getOccupiedBy().isEmpty())
                || trackDiagram.get(nextSection).isAllowMultipleOccupancy();
    }

    public void clearTrackSection(int occupiedSection,Integer serviceId) {
        trackDiagram.get(occupiedSection).clearOccupiedBy(serviceId);
    }

    public void occupySection(int eventSection, Integer id) {
        trackDiagram.get(eventSection).setOccupiedBy(id);
    }
}
