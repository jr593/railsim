package uk.co.raphel.railsim.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * * Created : 27/05/2017
 * * Author  : johnr
 **/
@Getter @Setter
public class Destination {
    private int id;
    private String name;
    private List<DestinationBoard> destinationBoards;


}
