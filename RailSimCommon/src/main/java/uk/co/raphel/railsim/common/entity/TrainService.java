package uk.co.raphel.railsim.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Entity(name = "TrainService")
@Table(name = "train_service")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class TrainService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="start_time")
    private LocalTime startTime;
    @Column(name="origin")
     private String origin;
    @Column(name="destination")
    private String destination;
    @OneToMany(mappedBy = "service")
    private List<RouteStop> routePoints;
    @Column(name="service_class")
    private String serviceClass;
    @Column(name="engine")
    private String engine;


    public static TrainService dummy() {

        TrainService trainService = new TrainService();
        trainService.setStartTime(LocalTime.now());
        return trainService;
    }

    public String toString() {
        return "Service Start time : " + startTime +
                " Origin : " + origin + " Destination : " + destination + ", Route points= " + routePoints.size();

    }
}
