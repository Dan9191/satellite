package com.example.satellite.service;

import com.example.satellite.entity.Facility;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.SatelliteAreaSessionRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemoryObservanceService {

    private final SatelliteFacilitySessionRepository facilitySessionRepository;

    private final SatelliteAreaSessionRepository areaSessionRepository;

    public void makeShootingSessions(Satellite satellite){
        List<SatelliteAreaSession> areaSessions = areaSessionRepository.findBySatellite(satellite);
        areaSessions.stream()
                .sorted(Comparator.comparing(SatelliteAreaSession::getStartSessionTime))
                .forEach(sas -> {
                    SatelliteFacilitySession sfs = sas.getFacilitySession();
                    if(isShootingSession(sas) && !sas.isShot())
                        shoot(sas);
                    else if (sfs != null) {
                        transferData(sfs);
                        sfs.setTransferred(true);
                    }
                });
        areaSessionRepository.saveBatch(areaSessions);
    }

    public void shoot(SatelliteAreaSession areaSession){
        Satellite satellite = areaSession.getSatellite();
        Long sasFinalMemoryState = areaSession.getInitialMemoryState() + satellite.getSatelliteType().getShootingSpeed() *
                (long)areaSession.getDuration();
        if (sasFinalMemoryState > satellite.getSatelliteType().getTotalMemory())
            sasFinalMemoryState = satellite.getSatelliteType().getTotalMemory();
        setMemoryStatesShooting(areaSession, sasFinalMemoryState);
    }

    protected void setMemoryStatesShooting(SatelliteAreaSession areaSession, Long sasFinalMemoryState) {
        areaSession.setFinalMemoryState(sasFinalMemoryState);
        areaSession.setShot(true);
        Integer nextAreaSessionId = areaSessionRepository.findNextByTime(areaSession.getSatellite(), areaSession.getEndSessionTime());
        if (nextAreaSessionId != 0) {
            areaSessionRepository.findById(nextAreaSessionId)
                    .ifPresent(sas -> {
                        sas.setInitialMemoryState(areaSession.getFinalMemoryState());
                        //satelliteAreaSessionRepository.save(sas);
                    });
        } else {
            System.out.printf("The end of scheduled period for %s is reached.\n", areaSession .getSatellite().getName());
        }
    }


    public boolean isShootingSession(SatelliteAreaSession areaSession){
        if (areaSession.getInitialMemoryState() == null)
            areaSession.setInitialMemoryState(0L);
        return areaSession.getStartSessionTime().toLocalTime()
                .isAfter(LocalTime.parse("09:00:00"))
                && areaSession.getEndSessionTime().toLocalTime()
                .isBefore(LocalTime.parse("18:00:00"))
                && areaSession.getInitialMemoryState() <
                areaSession.getSatellite().getSatelliteType().getTotalMemory() * 0.75;
    }

    public void makeTransferringSessions(Facility facility, Satellite satellite){
        List<SatelliteFacilitySession> facilitySessions = facilitySessionRepository.findByFacilityAndSatellite(facility, satellite);
        facilitySessions.stream()
                .sorted(Comparator.comparing(SatelliteFacilitySession::getStartSessionTime))
                .forEach(sfs -> {
                    SatelliteAreaSession sas = sfs.getAreaSession();
                    if ((sas == null ||
                            !isShootingSession(sas)) &&
                            sfs.getInitialMemoryState() > 0 && !sfs.isTransferred()){
                        transferData(sfs);
                    } else if (sas != null && isShootingSession(sas)){
                        shoot(sas);
                    }
                });
        facilitySessionRepository.saveBatch(facilitySessions);
    }
    public void transferData(SatelliteFacilitySession facilitySession){
        Satellite satellite = facilitySession.getSatellite();
        Long sfsFinalMemoryState = facilitySession.getInitialMemoryState() -
                satellite.getSatelliteType().getDataTransferSpeed() *
                        (long)facilitySession.getDuration();
        if (sfsFinalMemoryState < 0)
            sfsFinalMemoryState = 0L;
        setMemoryStatesTransferring(facilitySession, sfsFinalMemoryState);
    }

    protected void setMemoryStatesTransferring(SatelliteFacilitySession facilitySession, Long sfsFinalMemoryState) {
        facilitySession.setFinalMemoryState(sfsFinalMemoryState);
        facilitySession.setTransferred(true);
        Integer nextAreaSessionId = areaSessionRepository.findNextByTime(facilitySession.getSatellite(), facilitySession.getEndSessionTime());
        if (nextAreaSessionId != 0) {
            areaSessionRepository.findById(nextAreaSessionId)
                    .ifPresent(sas -> {
                        sas.setInitialMemoryState(facilitySession.getFinalMemoryState());
                        //satelliteAreaSessionRepository.save(sas);
                    });
        } else {
            System.out.printf("The end of scheduled period for %s is reached.\n", facilitySession.getSatellite().getName());
        }
    }

    public Long getTransferredDataVolume(SatelliteFacilitySession facilitySession){
        Long transferredDataVolume = (facilitySession.getInitialMemoryState() - facilitySession.getFinalMemoryState())/8388608;
        if (transferredDataVolume <= 0)
            return 0L;
        return transferredDataVolume;
    }
}
