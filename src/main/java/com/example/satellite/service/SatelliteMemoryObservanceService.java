package com.example.satellite.service;

import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.SatelliteAreaSessionRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SatelliteMemoryObservanceService {

    private final SatelliteAreaSessionRepository satelliteAreaSessionRepository;

    private final SatelliteFacilitySessionRepository satelliteFacilitySessionRepository;

    public void makeShootingSessions(Satellite satellite){

        List<SatelliteAreaSession> allAreaSessions = satelliteAreaSessionRepository.findBySatellite(satellite);
        allAreaSessions.stream()
                .filter(this::isShootingSession)
                .forEach(sas -> {
                    if (sas.getInitialMemoryState() == null)
                        sas.setInitialMemoryState(0L);
                    if (sas.getFinalMemoryState() == null)
                        sas.setFinalMemoryState(0L);
                    shoot(sas);}
                );
        satelliteAreaSessionRepository.saveBatch(allAreaSessions);
    }
    public List<SatelliteFacilitySession> makeTransferSessions(List<SatelliteFacilitySession> facilitySchedule){
        facilitySchedule.stream()
                .filter(sfs -> sfs.getAreaSession() != null && sfs.getAreaSession().getId() != 0)
                .forEach(sfs -> {
                    if (sfs.getAreaSession().getInitialMemoryState() == null)
                        sfs.getAreaSession().setInitialMemoryState(0L);
                    if (sfs.getAreaSession().getFinalMemoryState() == null)
                        sfs.getAreaSession().setFinalMemoryState(0L);
                    if (!isShootingSession(sfs.getAreaSession()) &&
                            sfs.getAreaSession().getInitialMemoryState() > 0)
                        transferData(sfs);
                });

        satelliteFacilitySessionRepository.saveBatch(facilitySchedule);

        return facilitySchedule;
    }

    public void shoot(SatelliteAreaSession areaSession){
        Satellite satellite = areaSession.getSatellite();
        Long sasFinalMemoryState = areaSession.getInitialMemoryState() + satellite.getSatelliteType().getShootingSpeed() *
                (long)areaSession.getDuration();
        if (sasFinalMemoryState > satellite.getSatelliteType().getTotalMemory())
            sasFinalMemoryState = satellite.getSatelliteType().getTotalMemory();
        setMemoryStates(areaSession, sasFinalMemoryState);
    }

    public void transferData(SatelliteFacilitySession facilitySession){
        Satellite satellite = facilitySession.getSatellite();
        SatelliteAreaSession facilityAreaSession = facilitySession.getAreaSession();
        Long sasFinalMemoryState = facilityAreaSession.getInitialMemoryState() -
                satellite.getSatelliteType().getDataTransferSpeed() *
                        (long)facilitySession.getDuration();
        if (sasFinalMemoryState < 0)
            sasFinalMemoryState = 0L;
        setMemoryStates(facilityAreaSession, sasFinalMemoryState);
    }

    private void setMemoryStates(SatelliteAreaSession areaSession, Long sasFinalMemoryState) {
        areaSession.setFinalMemoryState(sasFinalMemoryState);
        Integer nextAreaSessionId = satelliteAreaSessionRepository.findNextByTime(areaSession);
        if (nextAreaSessionId != 0) {
        satelliteAreaSessionRepository.findById(nextAreaSessionId)
                        .ifPresent(sas -> {
                            sas.setInitialMemoryState(areaSession.getFinalMemoryState());
                            satelliteAreaSessionRepository.save(sas);
                        });
        } else {
            System.out.println("The end of scheduled period is reached.");
        }
    }


    public boolean isShootingSession(SatelliteAreaSession areaSession){
        if (areaSession.getInitialMemoryState() == null)
            areaSession.setInitialMemoryState(0L);
        return areaSession.getStartSessionTime().toLocalTime()
                .isAfter(LocalTime.parse("09:00:00"))
                && areaSession.getEndSessionTime().toLocalTime()
                .isBefore(LocalTime.parse("18:00:00"))
                && areaSession.getInitialMemoryState() < areaSession.getSatellite().getSatelliteType().getTotalMemory() * 0.75;
    }


    public Long getTransferredDataVolume(SatelliteFacilitySession facilitySession){
        SatelliteAreaSession areaSession = facilitySession.getAreaSession();
        if (areaSession == null)
            return 0L;
        Long transferredDataVolume = (areaSession.getInitialMemoryState() - areaSession.getFinalMemoryState())/8388608;
        if (transferredDataVolume <= 0)
            return 0L;
        return transferredDataVolume;
    }
}
