package uk.co.raphel.railsim.common.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.raphel.railsim.common.entity.Berth;
import uk.co.raphel.railsim.common.enums.TrackDirection;

import java.util.ArrayList;
import java.util.List;

/*
   Used by track Mnagaer to hold live berth data
 */
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class LiveBerth {

     private Long berthId;
    private String berthName;
    private TrackDirection direction;
    private Double milesFromOrigin;
    private Double lengthMiles;
    private Boolean multiOccupancy;
    private Integer speedLimit;
    private List<String> pathTo;

    private List<RunningService> occupiers;

}
