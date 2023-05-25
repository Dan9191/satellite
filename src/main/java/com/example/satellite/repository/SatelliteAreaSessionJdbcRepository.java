package com.example.satellite.repository;

import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;

import java.time.LocalDateTime;
import java.util.List;

public interface SatelliteAreaSessionJdbcRepository {

    /**
     * Пакетное сохранение сеансов связи.
     *
     * @param sessionList список сеансов
     */
    void saveBatch(List<SatelliteAreaSession> sessionList);

    Integer findByTimeOverlap(Satellite satellite, LocalDateTime start,
                                                     LocalDateTime stop);

    Integer findNextByTime(SatelliteAreaSession areaSession);

}
