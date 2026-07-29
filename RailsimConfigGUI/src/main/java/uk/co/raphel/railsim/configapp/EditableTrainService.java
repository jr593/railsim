package uk.co.raphel.railsim.configapp;

import lombok.Getter;
import lombok.Setter;
import uk.co.raphel.railsim.common.entity.Berth;

import java.util.ArrayList;
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

    // Format is key = index in csv line, value = entry like "S03.23"
    private Map<Integer,String> callingPoints = new HashMap<>();


    public String toString() {
        return startTime + "-" + startName  + "(" +  startBerth.getBerthName() + ") " + destinationName + " (" + destBerth.getBerthName() + ")";
    }
    EditableTrainService() {
    }


    boolean hasNoEntries() {
        return getCallingPoints() == null ||
                getCallingPoints().isEmpty() ||
                callingPoints.isEmpty();
    }
}
