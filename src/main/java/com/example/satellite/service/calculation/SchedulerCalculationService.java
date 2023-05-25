package com.example.satellite.service.calculation;

import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.repository.SatelliteAreaSessionRepository;
import com.example.satellite.repository.SatelliteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.satellite.utils.ConstantUtils.END_SHOOTING_SESSION;
import static com.example.satellite.utils.ConstantUtils.START_SHOOTING_SESSION;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerCalculationService {

    private final SatelliteRepository satelliteRepository;

    private final SatelliteAreaSessionRepository satelliteAreaSessionRepository;

    /**
     * Набросок метода для вычисления времени конца памяти у каждого из спутников.
     */
    @Transactional
    public void  findFasterSatellite() {
        List<Satellite> satelliteList = satelliteRepository.findAll();
        log.info("Start calculated end memory time {}", LocalDateTime.now());
        satelliteList.forEach(satellite -> {
            List<SatelliteAreaSession> areaSessions = satelliteAreaSessionRepository.findAllBySatelliteOrderByStartSessionTime(satellite);
            LocalDateTime endMemoryDate = null;
                    if (satellite.getName().equals("KinoSat_111306")){
                        System.out.println("2222");
                    }
            long currentMemory = satellite.getSatelliteType().getTotalMemory();
            long shootingMemorySpeed = satellite.getSatelliteType().getShootingSpeed();
            long duration = 0;
            for (SatelliteAreaSession session: areaSessions) {

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
                    System.out.println(satellite.getName());
                    System.out.println(endMemoryDate);
                    System.out.println(duration);
                    System.out.println("---------");

        }
        );
        log.info("End calculated end memory time {}", LocalDateTime.now());

    }

    public boolean isShootingSession(SatelliteAreaSession areaSession){
        return areaSession.getStartSessionTime().toLocalTime().isAfter(START_SHOOTING_SESSION)
                && areaSession.getEndSessionTime().toLocalTime().isBefore(END_SHOOTING_SESSION);
    }
}
