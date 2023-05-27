package com.example.satellite.repository;

import com.example.satellite.entity.Area;
import com.example.satellite.entity.Satellite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {

    Optional<Area> findByName(String name);
}
