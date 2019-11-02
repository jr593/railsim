package uk.co.raphel.railsim.client;

import java.util.List;

/**
 * * Created : 27/05/2017
 * * Author  : johnr
 **/
public class Destination {
    private int id;
    private String name;
    private List<DestinationBoard> destinationBoards;

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    List<DestinationBoard> getDestinationBoards() {
        return destinationBoards;
    }

    void setDestinationBoards(List<DestinationBoard> destinationBoards) {
        this.destinationBoards = destinationBoards;
    }
}
