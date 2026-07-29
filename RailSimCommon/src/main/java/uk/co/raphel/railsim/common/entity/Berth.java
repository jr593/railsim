package uk.co.raphel.railsim.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.raphel.railsim.common.enums.TrackDirection;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Berth")
@Table(name = "berths")
public class Berth {

    @Id
    @Column(name="berth_id",insertable=false, updatable=false)
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
    @ManyToMany
    @JoinTable(
            name = "berth_connections",
            joinColumns = @JoinColumn(name = "from_berth_id"),
            inverseJoinColumns = @JoinColumn(name = "to_berth_id")
    )
    private List<Berth> pathTo = new ArrayList<>();


    public Berth(String part) {
        this.berthId = Long.parseLong(part);
        this.direction = TrackDirection.TERMINUS;
    }

    public void updateFrom(Berth realBerth) {
        berthId = realBerth.berthId;
        berthName = realBerth.berthName;
        direction = realBerth.direction;
        milesFromOrigin = realBerth.milesFromOrigin;
        lengthMiles = realBerth.lengthMiles;
        multiOccupancy = realBerth.multiOccupancy;
        speedLimit = realBerth.speedLimit;
        pathTo = new ArrayList<>(realBerth.pathTo);

    }
}
