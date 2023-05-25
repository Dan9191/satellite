package com.example.satellite.service;

import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.repository.SatelliteAreaSessionRepository;
import com.example.satellite.repository.SatelliteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SatelliteMemoryObservanceService {

    private final SatelliteAreaSessionRepository satelliteAreaSessionRepository;

    private final SatelliteRepository satelliteRepository;

    private List<SatelliteAreaSession> allAreaSessions = new ArrayList<>();

    public List<SatelliteAreaSession> evaluateSessions(String satelliteName){

        Satellite satellite = satelliteRepository.findByName(satelliteName).orElseThrow();

        allAreaSessions = satelliteAreaSessionRepository.findBySatellite(satellite);
        allAreaSessions.stream()
                .filter(sas -> sas.getStartSessionTime().toLocalTime()
                        .isAfter(LocalTime.parse("09:00:00"))
                        && sas.getEndSessionTime().toLocalTime()
                        .isAfter(LocalTime.parse("18:00:00")))
                .forEach(sas -> sas.setFinalMemoryState( satellite.getSatelliteType().getShootingSpeed() *
                                (long)sas.getDuration()));
        for (int i = 0; i < allAreaSessions.size(); i++){
            if (allAreaSessions.get(i).getFinalMemoryState() != null && i != allAreaSessions.size()-1)
                allAreaSessions.get(i+1).setInitialMemoryState(allAreaSessions.get(i).getFinalMemoryState());
        }
        return allAreaSessions;
    }
}
