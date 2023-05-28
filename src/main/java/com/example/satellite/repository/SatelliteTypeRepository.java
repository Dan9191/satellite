package com.example.satellite.repository;

import com.example.satellite.entity.SatelliteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SatelliteTypeRepository extends JpaRepository<SatelliteType, Integer> {

}
