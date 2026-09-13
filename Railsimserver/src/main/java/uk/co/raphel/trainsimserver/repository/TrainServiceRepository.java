package uk.co.raphel.trainsimserver.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.co.raphel.railsim.common.entity.TrainService;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface TrainServiceRepository extends JpaRepository<TrainService, Long> {


    @Query("""
    SELECT r
    FROM TrainService r
    ORDER BY
        CASE
            WHEN r.startTime >= :startTime THEN 0
            ELSE 1
        END,
        r.startTime ASC """)
    List<TrainService> findNextDepartures(
            @Param("startTime") LocalTime currentTime,
            Pageable pageable
    );

}
