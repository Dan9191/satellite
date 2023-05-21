package com.example.satellite.repository;

import com.example.satellite.entity.Facility;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteFacilitySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SatelliteFacilitySessionRepository extends JpaRepository<SatelliteFacilitySession, Integer>, SatelliteFacilitySessionJdbcRepository {

    public List<SatelliteFacilitySession> findByFacilityAndSatellite(Facility facility,
                                                                     Satellite satellite);
}
