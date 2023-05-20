package com.example.satellite.repository;

import com.example.satellite.entity.SatelliteAreaSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SatelliteAreaSessionRepository extends JpaRepository<SatelliteAreaSession, Integer>, SatelliteAreaSessionJdbcRepository {
}
