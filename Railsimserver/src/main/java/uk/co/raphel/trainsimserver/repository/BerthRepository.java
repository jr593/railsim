package uk.co.raphel.trainsimserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.co.raphel.railsim.common.entity.Berth;

import java.util.List;

@Repository
public interface BerthRepository extends JpaRepository<Berth, Long> {

        @Query("""
        SELECT DISTINCT b
        FROM Berth b
        LEFT JOIN FETCH b.pathTo
    """)
        List<Berth> findAllWithPathTo();
    }

    


