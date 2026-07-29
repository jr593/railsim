package uk.co.raphel.railsim.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.raphel.railsim.common.enums.TrackDirection;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Destination")
@Table(name = "destination")
public class Destination {

    @Id
    @Column(name = "destination_number")
    private Integer destinationNumber;
    @Column(name = "name")
    private String name;
    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )    private Set<Berth> homeBerths = new HashSet<>();

    // index	Name	Home Base 1	HomeBase 2	HomeBase 3	HomeBase 4	HomeBase 5	HomeBase 6
    public Destination(String line) {
        String[] parts = line.split(",",-1);
        this.destinationNumber = Integer.parseInt(parts[0]);
        this.name = parts[1];
        for(int i = 2; i< parts.length; i++) {
            if (!parts[i].isEmpty()) {
                homeBerths.add(new Berth(parts[i]));
            }
        }
    }

}
