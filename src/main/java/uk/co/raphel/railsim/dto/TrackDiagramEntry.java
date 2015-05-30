package uk.co.raphel.railsim.dto;/**
 * Created by johnr on 30/05/2015.
 */

import java.io.Serializable;

/**
 * * Created : 30/05/2015
 * * Author  : johnr
 *
 * This class represents a Track Diagram entry. That is, a osition
 * on the map that a running service can occupy
 **/
public class TrackDiagramEntry implements Serializable {

    private Integer id;
    private String name;
    private double length; // In miles
    private int speedLimit;
    private int occupiedBy; // Id of service currently occupying section


    public TrackDiagramEntry(String csvLine) {

        //Take line form imnput resource to construct me.

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(int speedLimit) {
        this.speedLimit = speedLimit;
    }

    public int getOccupiedBy() {
        return occupiedBy;
    }

    public void setOccupiedBy(int occupiedBy) {
        this.occupiedBy = occupiedBy;
    }
}
