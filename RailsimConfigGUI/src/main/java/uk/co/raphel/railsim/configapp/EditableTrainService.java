package uk.co.raphel.railsim.configapp;

import lombok.Getter;
import lombok.Setter;
import uk.co.raphel.railsim.common.entity.Berth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 **/
@Setter @Getter
public class EditableTrainService {

    private String startTime;
    private Berth startBerth;
    private String startName;
    private Berth destBerth;
    private String destinationName;
    private String serviceClass;
    private String engine;


    private Map<Integer, String> callingPoints = new HashMap<>();


    public String toString() {
        return startTime + "-" + startName  + "(" +  startBerth.getBerthName() + ") " + destinationName + " (" + destBerth.getBerthName() + ")";
    }
    EditableTrainService() {
    }


    boolean hasNoEntries() {
        return getCallingPoints() == null ||
                getCallingPoints().isEmpty() ||
                callingPoints.values()
                        .stream()
                        .noneMatch(m -> m != null && m.length() > 0);
    }


    Map<Integer, String> getCallingPoints() {
        return callingPoints;
    }

    void setCallingPoints(Map<Integer, String> callingPoints) {
        this.callingPoints = callingPoints;
    }
}
