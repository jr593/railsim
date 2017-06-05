package uk.co.raphel.railsim.client;/**
 * Created by johnr on 27/05/2017.
 */

import java.util.List;

/**
 * * Created : 27/05/2017
 * * Author  : johnr
 **/
public class Destination {
    int id;
    String name;
    List<DestinationBoard> destinationBoards;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DestinationBoard> getDestinationBoards() {
        return destinationBoards;
    }

    public void setDestinationBoards(List<DestinationBoard> destinationBoards) {
        this.destinationBoards = destinationBoards;
    }
}
