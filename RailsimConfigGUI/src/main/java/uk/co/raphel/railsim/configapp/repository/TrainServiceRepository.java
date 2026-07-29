package uk.co.raphel.railsim.configapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.raphel.railsim.common.entity.Berth;
import uk.co.raphel.railsim.common.entity.TrainService;

@Repository
public interface TrainServiceRepository extends JpaRepository<TrainService, Long> {
}
