package com.example.satellite.service;

import com.example.satellite.config.SatelliteProperties;
import com.example.satellite.entity.Facility;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.FacilityRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class GreedyFacilityScheduleService {

    private final FacilityRepository facilityRepository;

    private final SatelliteFacilitySessionRepository satelliteFacilitySessionRepository;

    private List<SatelliteFacilitySession> allSessionsList= new ArrayList<>();

    public List<SatelliteFacilitySession> makeFacilitySchedule(String facilityName){
        Facility facility = facilityRepository.findByName(facilityName)
                .orElseThrow(() -> {
                    log.error("Facility '{}' not found", facilityName);
                    throw new RuntimeException(String.format("Наземное устройство '%s' не найдено", facilityName));
                });

        allSessionsList = satelliteFacilitySessionRepository.findByFacilityOrderByStartSessionTime(facility);

        if (allSessionsList.isEmpty()) {
            log.error("Facility '{}' not found", facilityName);
            throw new RuntimeException(String.format("У наземного устройство '%s' отсутствуют принимаемые спутники",
                    facilityName));
        }
        SatelliteFacilitySession firstSession = allSessionsList.get(0);

        List<SatelliteFacilitySession> facilitySchedule = new ArrayList<>();
        while (!allSessionsList.isEmpty()){
            SatelliteFacilitySession nextSession = getNext(firstSession);
            if (nextSession.getId() == null)
                break;
            facilitySchedule.add(nextSession);
            firstSession = facilitySchedule.get(facilitySchedule.size() -1);
        }
        return facilitySchedule;
    }

    private SatelliteFacilitySession getNext(SatelliteFacilitySession session){
        LocalDateTime previousSession = session.getEndSessionTime();
        if (allSessionsList.get(allSessionsList.size()-1).getStartSessionTime().isBefore(previousSession)){
            allSessionsList = new ArrayList<>();
            return new SatelliteFacilitySession();
        }
        SatelliteFacilitySession nextSession = allSessionsList.stream()
                .filter(sfs -> sfs.getStartSessionTime().isAfter(previousSession))
                .min(Comparator.comparing(SatelliteFacilitySession::getStartSessionTime))
                .orElseThrow();
        allSessionsList = allSessionsList.stream()
                .filter(s -> s.getStartSessionTime().isAfter(nextSession.getStartSessionTime()))
                .toList();
        return nextSession;
    }


}
