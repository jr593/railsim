package uk.co.raphel.railsim.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.raphel.railsim.common.enums.TrackDirection;

@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Berth")
@Table(name = "berths")
public class Berth {

    @Id
    @Column(name="berth_id")
    private Long berthId;
    @Column(name="berth_name")
    private String berthName;
    @Column(name = "direction")
    private TrackDirection direction;
    @Column(name="miles_from_orig")
    private Double milesFromOrigin;
    @Column(name="length_miles")
    private Double lengthMiles;
    @Column(name = "multi_occ")
    private Boolean multiOccupancy;
    @Column(name="speed_limit")
    private Integer speedLimit;

    @ManyToOne
    @JoinColumn(name = "destination_number")
    private Destination homeBase;

    public Berth(String part) {
    }

}
