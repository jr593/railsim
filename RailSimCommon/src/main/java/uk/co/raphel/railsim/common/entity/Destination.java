package uk.co.raphel.railsim.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
            mappedBy = "homeBase",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )    private Set<Berth> homeBerths = new HashSet<>();

    // index,Name,Mark1,Mark2,MArk3,Mark4,Mark5,Mark6
    public Destination(String line) {
        String[] parts = line.split(",",-1);
        this.destinationNumber = Integer.parseInt(parts[0]);
        this.name = parts[1];
        for(int i = 2; i<8; i++) {
            if (parts[i].length() > 0) {
                addBerth(new Berth(parts[i]));
            }
        }
    }

    public void addBerth(Berth berth) {
        homeBerths.add(berth);
        berth.setHomeBase(this);
    }
}
