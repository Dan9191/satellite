package com.example.satellite.service;

import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.SatelliteAreaSessionRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FacilityToAreaReferenceService {

    private final SatelliteFacilitySessionRepository facilitySessionRepository;

    private final SatelliteAreaSessionRepository areaSessionRepository;

    private final GreedyFacilityScheduleService scheduleService;

    private final SatelliteMemoryObservanceService memoryObservanceService;

    public void referFacilitySessionToAreaSession(List<SatelliteFacilitySession> facilitySessionSchedule) {

        facilitySessionSchedule.parallelStream()
                .forEach(sfs -> {
                    Satellite sessionSatellite = sfs.getSatellite();
                    Integer areaSessionId = areaSessionRepository.findByTimeOverlap(sessionSatellite, sfs.getStartSessionTime(), sfs.getEndSessionTime());
                    if (areaSessionId == 0) {
                        System.out.printf("No satellite area session referring to facility %s session %d.\n", facilitySessionSchedule, sfs.getId());
                    } else {
                        SatelliteAreaSession areaSession = areaSessionRepository.findById(areaSessionId).orElseThrow();
                        sfs.setAreaSession(areaSession);
                        facilitySessionRepository.save(sfs);}
                    }
                );
    }
}
