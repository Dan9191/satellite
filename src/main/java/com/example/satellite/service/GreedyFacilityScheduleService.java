package com.example.satellite.service;

import com.example.satellite.entity.Facility;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.FacilityRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
@Slf4j

public class GreedyFacilityScheduleService {

    private final FacilityRepository facilityRepository;

    private final SatelliteFacilitySessionRepository satelliteFacilitySessionRepository;

    private List<SatelliteFacilitySession> allSessionsList;

    @Autowired
    public GreedyFacilityScheduleService(FacilityRepository facilityRepository,
                                         SatelliteFacilitySessionRepository satelliteFacilitySessionRepository) {
        this.facilityRepository = facilityRepository;
        this.satelliteFacilitySessionRepository = satelliteFacilitySessionRepository;
    }

    public List<SatelliteFacilitySession> makeFacilitySchedule(String facilityName){
        Facility facility = facilityRepository.findFirstByName(facilityName)
                .orElseThrow();
        allSessionsList = new ArrayList<>();
            allSessionsList = satelliteFacilitySessionRepository.findByFacility(facility)
                    .stream()
                    .sorted(Comparator.comparing(SatelliteFacilitySession::getStartSessionTime))
                    .toList();

        SatelliteFacilitySession firstSession = allSessionsList.stream()
                .min(Comparator.comparing(SatelliteFacilitySession::getStartSessionTime))
                .orElseThrow();

        List<SatelliteFacilitySession> facilitySchedule = new ArrayList<>();
        while (!allSessionsList.isEmpty()){
            facilitySchedule.add(getNext(firstSession));
            firstSession = facilitySchedule.get(facilitySchedule.size() -1);
        }
        return facilitySchedule;
    }

    private SatelliteFacilitySession getNext(SatelliteFacilitySession session){
        LocalDateTime previousSession = session.getEndSessionTime();
        if (allSessionsList.get(allSessionsList.size()-1).getStartSessionTime().isBefore(previousSession)){
            allSessionsList = new ArrayList<>();
            return new SatelliteFacilitySession();}
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
