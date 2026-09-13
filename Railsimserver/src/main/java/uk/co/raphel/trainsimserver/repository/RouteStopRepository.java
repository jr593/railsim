package uk.co.raphel.trainsimserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.raphel.railsim.common.entity.RouteStop;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
}
