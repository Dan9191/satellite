package com.example.satellite.repository;

import com.example.satellite.entity.SatelliteFacilitySession;

import java.util.List;

public interface SatelliteFacilitySessionJdbcRepository {

    /**
     * Пакетное сохранение сеансов связи.
     *
     * @param sessionList список сеансов
     */
    void saveBatch(List<SatelliteFacilitySession> sessionList);

}
