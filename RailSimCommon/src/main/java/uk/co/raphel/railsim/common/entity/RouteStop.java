package uk.co.raphel.railsim.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.co.raphel.railsim.common.enums.StopType;

import java.time.LocalTime;

@Entity(name = "RouteStop")
@Table(name = "route_stop")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "berth_id")
    private Berth berth;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private TrainService service;
    @Column(name="arrival_time")
    private LocalTime arrivalTime;
    @Column(name="departure_time")
    private LocalTime departureTime;
    @Column(name="stop_type")
    private StopType stopType;
    @Column(name = "route_stop_number")
    private Integer routeStopNumber;

}
