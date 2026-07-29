package uk.co.raphel.railsim.common;

import lombok.Getter;
import lombok.Setter;
import uk.co.raphel.railsim.common.enums.TrackDirection;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 * <p>
 * This class represents a Track Diagram entry. That is, a position
 * on the map that a running service can occupy
 **/
@Getter @Setter
public class TrackDiagramEntry implements Serializable {

    private Integer id; // Will be the berth id of this track section
    private String name;
    private double length; // In miles
    private double dist; // From origin in miles
    private int speedLimit;
    private Set<Integer> occupiedBy; // Id of service currently occupying section
    private boolean allowMultipleOccupancy;
    private TrackDirection trackDirection;
    private Set<Integer> linkedBerths;


    public TrackDiagramEntry(String csvLine) {

        //Take line form imnput resource to construct me.
        // e.g. 1,Victoria Down Main,0.00,0.00
        String[] csv = csvLine.split(",",-1);
        if (csv.length >= 5) {
            this.id = Integer.parseInt(csv[0]);
            this.name = csv[1];
            this.dist = Double.parseDouble(csv[2]);
            this.length = Double.parseDouble(csv[3]);
            this.allowMultipleOccupancy = csv[4].equals("Y");
            this.trackDirection = csv[5]=="U" ? TrackDirection.UP : TrackDirection.DOWN;
            linkedBerths = new HashSet<>();
            for(int i=5; i< csv.length; i++) {
                try {
                    linkedBerths.add(Integer.parseInt(csv[i]));
                } catch(Exception e) {
                    // Do nothing
                }
            }
            // TODO: Enable this
            this.speedLimit = 40;
        }

    }


    public Set<Integer> getOccupiedBy() {
        if (this.occupiedBy == null) {
            this.occupiedBy = new HashSet<>();
        }
        return occupiedBy;
    }

    public void clearOccupiedBy(int serviceId) {
        if (this.occupiedBy == null) {
            this.occupiedBy = new HashSet<>();
        }
        this.occupiedBy.remove(serviceId);
    }

    public void setOccupiedBy(int occupiedBy) {
        if (this.occupiedBy == null) {
            this.occupiedBy = new HashSet<>();
        }
        this.occupiedBy.add(occupiedBy);
    }
}
