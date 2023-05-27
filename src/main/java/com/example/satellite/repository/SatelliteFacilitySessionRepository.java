package com.example.satellite.repository;

import com.example.satellite.entity.Facility;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SatelliteFacilitySessionRepository extends JpaRepository<SatelliteFacilitySession, Integer>, SatelliteFacilitySessionJdbcRepository {

    List<SatelliteFacilitySession> findByFacilityOrderByStartSessionTime(Facility facility);

    List<SatelliteFacilitySession> findByFacilityAndSatellite(Facility facility, Satellite satellite);

    Optional<SatelliteFacilitySession> findByAreaSession(SatelliteAreaSession areaSession);

    List<SatelliteFacilitySession> findByFacilityName(String facilityName);

}
