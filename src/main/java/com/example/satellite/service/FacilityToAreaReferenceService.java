package com.example.satellite.service;

import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.SatelliteAreaSessionRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class FacilityToAreaReferenceService {

    private final SatelliteFacilitySessionRepository facilitySessionRepository;

    private final SatelliteAreaSessionRepository areaSessionRepository;


    public void referFacilitySessionToAreaSession(List<SatelliteFacilitySession> facilitySessions) {

        List<SatelliteAreaSession> areaSessions = new ArrayList<>();
        String facilityName = facilitySessions.get(0).getFacility().getName();
        facilitySessions.parallelStream()
                .forEach(sfs -> {
                            Satellite sessionSatellite = sfs.getSatellite();
                            Integer areaSessionId = areaSessionRepository.findByTimeOverlap(sessionSatellite,
                                    sfs.getStartSessionTime(), sfs.getEndSessionTime());
                            if (areaSessionId == null) {
                                System.out.printf("No satellite area session referring to facility %s session %d.\n", facilityName, sfs.getId());
                            } else {
                                Optional <SatelliteAreaSession> foundAreaSession = areaSessionRepository.findById(areaSessionId);
                                if(foundAreaSession.isPresent()) {
                                    SatelliteAreaSession areaSession = foundAreaSession.get();
                                    sfs.setAreaSession(areaSession);
                                    areaSession.setFacilitySession(sfs);
                                    areaSessions.add(areaSession);
                                }
                            }
                        }
                );
        facilitySessionRepository.saveBatch(facilitySessions);
        areaSessionRepository.saveBatch(areaSessions);
    }
}
