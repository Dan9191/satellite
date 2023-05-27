package com.example.satellite.repository;

import com.example.satellite.entity.Satellite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы со спутником.
 */
@Repository
public interface SatelliteRepository extends JpaRepository<Satellite, Integer> {

    /**
     * Поиск спутника по названию.
     *
     * @param name Название спутника.
     * @return Спутник.
     */
    Optional<Satellite> findFirstByName(String name);
}
