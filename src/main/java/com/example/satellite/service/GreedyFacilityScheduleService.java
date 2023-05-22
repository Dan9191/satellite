package com.example.satellite.service;

import com.example.satellite.entity.Facility;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.FacilityRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class GreedyFacilityScheduleService {

    private final FacilityRepository facilityRepository;

    private final SatelliteFacilitySessionRepository satelliteFacilitySessionRepository;

    private List<SatelliteFacilitySession> allSessionsList;

    public Queue<SatelliteFacilitySession> makeFacilitySchedule(String facilityName){
        Facility facility = facilityRepository.findFirstByName(facilityName)
                .orElseThrow();
        allSessionsList = new ArrayList<>();
            allSessionsList.addAll(satelliteFacilitySessionRepository.findByFacility(facility)
                    .stream()
                    .sorted(Comparator.comparing(SatelliteFacilitySession::getStartSessionTime))
                    .toList());

        SatelliteFacilitySession firstSession = allSessionsList.stream()
                .min(Comparator.comparing(SatelliteFacilitySession::getStartSessionTime))
                .orElseThrow();

        Queue<SatelliteFacilitySession> facilitySchedule = new PriorityQueue<>();
        while (!allSessionsList.isEmpty()){
            facilitySchedule.add(getNext(allSessionsList, getNext(allSessionsList, firstSession)));
        }
        return facilitySchedule;
    }

    private SatelliteFacilitySession getNext(List<SatelliteFacilitySession> allSessions,
                                             SatelliteFacilitySession session){
        LocalDateTime previousSession = session.getEndSessionTime();
        SatelliteFacilitySession nextSession = allSessions.stream()
                .filter(sfs -> sfs.getStartSessionTime().isAfter(previousSession))
                .min(Comparator.comparing(SatelliteFacilitySession::getStartSessionTime))
                .orElseThrow();
        allSessionsList = allSessionsList.stream()
                .filter(s -> s.getStartSessionTime().isAfter(nextSession.getStartSessionTime()))
                .toList();
        return nextSession;
    }


}
