package uk.co.raphel.railsim.configapp;/**
 * Created by johnr on 30/05/2015.
 */

import uk.co.raphel.railsim.common.TrackDiagramEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
public class EditableTrainService {

    private String startTime;
    private String start;
    private String destination;
    private String serviceClass;
    private String engine;


    private Map<Integer, String> callingPoints = new HashMap<>();


    public String toString() {
        return startTime + "-" + start + " to " + destination;
    }
    public EditableTrainService(String csvLine, List<Integer> indexList) {
        // e.g
        // Train,From,Class,Engine,Destination,1,2,3,4,5,6,7,8,9,10,11,200,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,201,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81
       //  00:25,Victoria,Pass,EMU,,S00.25,,,,S00.31,,S00.35,,,S00.38,S00.40,,S00.44,S00.46,,,T00.48,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
        String csv[] = csvLine.split(",");

        startTime = csv[0];
        this.start = csv[1];
        this.destination =  csv[4];
        serviceClass = csv[2];
        engine = csv[3];

        for(int i = 5; i< csv.length; i++) {
               callingPoints.put(indexList.get(i-5), csv[i]);
        }
        if(csv.length-5 < indexList.size()) {
            for(int i=csv.length-5; i<indexList.size(); i++) {
                callingPoints.put(indexList.get(i),"");
            }
        }

    }


    public boolean hasNoEntries() {
        return getCallingPoints() == null ||
                getCallingPoints().isEmpty() ||
                !callingPoints.values()
                        .stream()
                        .filter(m -> m != null && m.length() > 0)
                        .findFirst()
                        .isPresent();
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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

    public Map<Integer, String> getCallingPoints() {
        return callingPoints;
    }

    public void setCallingPoints(Map<Integer, String> callingPoints) {
        this.callingPoints = callingPoints;
    }
}
