package com.example.satellite.repository;

import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сеансами съемки спутника.
 */
@Repository
public interface SatelliteAreaSessionRepository extends JpaRepository<SatelliteAreaSession, Integer>, SatelliteAreaSessionJdbcRepository {

    List<SatelliteAreaSession> findAllBySatelliteOrderByStartSessionTime(Satellite satellite);

}
