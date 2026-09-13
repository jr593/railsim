package uk.co.raphel.trainsimserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.raphel.railsim.common.entity.Berth;

@Repository
public interface BerthRepository extends JpaRepository<Berth, Long> {
}
