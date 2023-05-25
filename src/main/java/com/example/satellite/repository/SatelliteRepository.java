package com.example.satellite.repository;

import com.example.satellite.entity.Satellite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SatelliteRepository extends JpaRepository<Satellite, Integer> {

    Optional<Satellite> findFirstByName(String name);

    Optional<Satellite> findByName(String name);
}
