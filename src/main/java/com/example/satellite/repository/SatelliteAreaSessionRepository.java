package com.example.satellite.repository;

import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SatelliteAreaSessionRepository extends JpaRepository<SatelliteAreaSession, Integer>, SatelliteAreaSessionJdbcRepository {

}
