package com.example.satellite.repository;

import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SatelliteAreaSessionJdbcRepository {

    /**
     * Пакетное сохранение сеансов связи.
     *
     * @param sessionList список сеансов
     */
    void saveBatch(List<SatelliteAreaSession> sessionList);

    Integer findByTimeOverlap(Satellite satellite, LocalDateTime start,
                                                     LocalDateTime stop);

}
