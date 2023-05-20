package com.example.satellite.repository;

import com.example.satellite.entity.SatelliteFacilitySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SatelliteFacilitySessionRepository extends JpaRepository<SatelliteFacilitySession, Integer>, SatelliteFacilitySessionJdbcRepository {
}
