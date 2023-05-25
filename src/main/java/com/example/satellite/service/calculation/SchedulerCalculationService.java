package com.example.satellite.service.calculation;

import com.example.satellite.entity.Facility;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.FacilityRepository;
import com.example.satellite.repository.SatelliteAreaSessionRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import com.example.satellite.repository.SatelliteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.satellite.utils.ConstantUtils.END_SHOOTING_SESSION;
import static com.example.satellite.utils.ConstantUtils.START_SHOOTING_SESSION;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerCalculationService {

    private final SatelliteRepository satelliteRepository;

    private final FacilityRepository facilityRepository;

    private final SatelliteAreaSessionRepository satelliteAreaSessionRepository;

    private final SatelliteFacilitySessionRepository satelliteFacilitySessionRepository;

    /**
     * Набросок метода для вычисления времени конца памяти у каждого из спутников.
     */
    public void findFasterSatellite() {

        long startTime = System.currentTimeMillis();
        log.info("Start process {}", LocalDateTime.now());
        log.info("Start calculated facility schedule ");
        Map<Facility, List<SatelliteFacilitySession>> facilitySessionsMap = findAllFacilitySessionsMap();
        long stageTime = System.currentTimeMillis();
        log.info("End calculated facility schedule {} ms", stageTime - startTime);

        log.info("Start calculated area schedule");


        log.info("End calculated facility schedule {} ms", System.currentTimeMillis() - stageTime);
        log.info("End calculated end memory time {}", LocalDateTime.now());

    }

    /**
     * Собирает данные о всех сеансах связи спутника с назменым приемником.
     *
     * @return Мапа сеансов связи спутника с землей, где key - назменый приемник, value - список его сеансов.
     */
    private Map<Facility, List<SatelliteFacilitySession>> findAllFacilitySessionsMap() {
        List<Facility> facilityList = facilityRepository.findAll();
        Map<Facility, List<SatelliteFacilitySession>> facilityScheduleMap = new HashMap<>();
        facilityList.forEach(facility -> {
                    List<SatelliteFacilitySession> facilitySessions = satelliteFacilitySessionRepository
                            .findByFacilityOrderByStartSessionTime(facility);
                    facilityScheduleMap.put(facility, facilitySessions);
                }
        );
        return facilityScheduleMap;
    }

    /**
     * Собирает данные о всех сеансах связи спутника с землей.
     *
     * @return Мапа сеансов съемки спутника, где key - спутник, value - сеансы съемки.
     */
    private Map<Satellite, List<SatelliteAreaSession>> findAllSatelliteAreaSessionsMap() {
        List<Satellite> satelliteList = satelliteRepository.findAll();
        Map<Satellite, List<SatelliteAreaSession>> areaScheduleMap = new HashMap<>();
        satelliteList.forEach(satellite -> {
                    List<SatelliteAreaSession> areaSessions = satelliteAreaSessionRepository
                            .findAllBySatelliteOrderByStartSessionTime(satellite);
                    LocalDateTime endMemoryDate = null;
                    long currentMemory = satellite.getSatelliteType().getTotalMemory();
                    long shootingMemorySpeed = satellite.getSatelliteType().getShootingSpeed();
                    long duration = 0;
                    for (SatelliteAreaSession session : areaSessions) {

                        if (currentMemory <= 0L) {
                            break;
                        }
                        if (isShootingSession(session)) {
                            duration += (long) session.getDuration();
                            long sessionMemorySpending = (long) session.getDuration() * shootingMemorySpeed;
                            currentMemory -= sessionMemorySpending;
                            endMemoryDate = session.getEndSessionTime();
                        }
                    }
                    areaScheduleMap.put(satellite, areaSessions);
                    //вывод информации о спутнике и времени, когда он заполнит память
                    System.out.println(satellite.getName());
                    System.out.println(endMemoryDate);
                    System.out.println(duration);
                    System.out.println("---------");
                }
        );
        return areaScheduleMap;
    }

    /**
     * Признак наличия света для съемки.
     *
     * @param areaSession Сеанс съемки.
     * @return есть ли в это время освещение.
     */
    public boolean isShootingSession(SatelliteAreaSession areaSession) {
        return areaSession.getStartSessionTime().toLocalTime().isAfter(START_SHOOTING_SESSION)
                && areaSession.getEndSessionTime().toLocalTime().isBefore(END_SHOOTING_SESSION);
    }
}
