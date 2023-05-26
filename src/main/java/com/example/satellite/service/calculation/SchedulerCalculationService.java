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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.example.satellite.utils.ConstantUtils.IS_SHOOTING_TIME;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerCalculationService {

    /**
     * Репозиторий для работы со спутником.
     */
    private final SatelliteRepository satelliteRepository;

    /**
     * Репозиторий для работы с приемником.
     */
    private final FacilityRepository facilityRepository;

    /**
     * Репозиторий для работы с сеансами съемки спутника.
     */
    private final SatelliteAreaSessionRepository satelliteAreaSessionRepository;

    /**
     * Репозиторий для работы с сеансами связи спутника и приемника земли.
     */
    private final SatelliteFacilitySessionRepository satelliteFacilitySessionRepository;

    /**
     * Список доступных сеансов связи спутника и приемника земли.
     */
    private Map<Facility, List<SatelliteFacilitySession>> freeFacilitySessionsMap = new HashMap<>();

    /**
     * Список сеансов связи спутника и приемника земли, которые произойдут реально.
     */
    private Map<Facility, List<SatelliteFacilitySession>> actualFacilitySessionsMap = new HashMap<>();

    /**
     * Набросок метода для вычисления времени конца памяти у каждого из спутников.
     */
    public void findFasterSatellite() {

        long startTime = System.currentTimeMillis();
        log.info("Start process {}", LocalDateTime.now());
        log.info("Start calculated facility schedule ");
        freeFacilitySessionsMap = findAllFacilitySessionsMap();
        long stageTime = System.currentTimeMillis();
        log.info("End calculated facility schedule {} ms", stageTime - startTime);

        log.info("Start calculated area schedule");
        Map<Satellite, List<SatelliteAreaSession>> SatelliteAreaSessionsMap = findAllSatelliteAreaSessionsMap();
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
     * Собирает данные о всех сеансах съемки спутника с 9:00 до 18:00.
     *
     * @return Мапа сеансов съемки спутника, где key - спутник, value - сеансы съемки.
     */
    private Map<Satellite, List<SatelliteAreaSession>> findAllSatelliteAreaSessionsMap() {
        List<Satellite> satelliteList = satelliteRepository.findAll();
        Map<Satellite, List<SatelliteAreaSession>> areaScheduleMap = new HashMap<>();
        satelliteList.forEach(satellite -> {
            List<SatelliteAreaSession> areaSessions =
                    satelliteAreaSessionRepository.findAllBySatelliteOrderByStartSessionTime(satellite).stream()
                            .filter(IS_SHOOTING_TIME)
                            .toList();
                    areaScheduleMap.put(satellite, areaSessions);
                }
        );
        return areaScheduleMap;
    }

    /**
     * Вычисляет время переполнения памяти.
     *
     *  @param satelliteAreaSessionsMap Мапа сеансов съемки спутника, где key - спутник, value - сеансы съемки.
     */
    private void memoryOverflowTime(Map<Satellite, List<SatelliteAreaSession>> satelliteAreaSessionsMap) {
//        satelliteAreaSessionsMap.forEach((key, value) -> {
//            value.forEach(satellite -> {
//                List<SatelliteAreaSession> areaSessions = satelliteAreaSessionRepository
//                        .findAllBySatelliteOrderByStartSessionTime(satellite);
//                LocalDateTime endMemoryDate = null;
//                long currentMemory = satellite.getSatelliteType().getTotalMemory();
//                long shootingMemorySpeed = satellite.getSatelliteType().getShootingSpeed();
//                long duration = 0;
//                for (SatelliteAreaSession session : areaSessions) {
//
//                    if (currentMemory <= 0L) {
//                        break;
//                    }
//                    if (isShootingSession(session)) {
//                        duration += (long) session.getDuration();
//                        long sessionMemorySpending = (long) session.getDuration() * shootingMemorySpeed;
//                        currentMemory -= sessionMemorySpending;
//                        endMemoryDate = session.getEndSessionTime();
//                    }
//                }
//
//
//                areaScheduleMap.put(satellite, areaSessions);
//                //вывод информации о спутнике и времени, когда он заполнит память
//                System.out.println(satellite.getName());
//                System.out.println(endMemoryDate);
//                System.out.println(duration);
//                System.out.println("---------");
//            }
//        });
//
    }

    /**
     * Вычисление подходящего сеанса связи для спутника и земли.
     *
     * @param satellite         Спутник, для которого ищем расписание.
     * @param startFreeInterval Начала интервала, в который спутник доступен для передачи данных.
     * @param endFreeInterval   Конец интервала, в который спутник доступен для передачи данных.
     * @return Сеанс связи спутника и земли
     */
    private Optional<SatelliteFacilitySession> findSatelliteFacilitySession(Satellite satellite,
                                                                  LocalDateTime startFreeInterval,
                                                                  LocalDateTime endFreeInterval) {
        //поиск подходящих сеансов
        List<SatelliteFacilitySession> concurrentSessionList = new ArrayList<>();
        freeFacilitySessionsMap.forEach((key, value) -> {
            Optional<SatelliteFacilitySession> optSession = value.stream()
                    .filter(session -> session.getSatellite().equals(satellite))
                    .filter(session -> session.getStartSessionTime().isAfter(startFreeInterval))
                    .filter(session -> session.getStartSessionTime().isBefore(endFreeInterval))
                    .findFirst();
            optSession.ifPresent(concurrentSessionList::add);
        });
        //если не удалось найти запись - вернем пустой Optional
        if (concurrentSessionList.isEmpty()) {
            return Optional.empty();
        }
        //выбор сеанса, который наступит раньше всех
        concurrentSessionList.sort(Comparator.comparing(SatelliteFacilitySession::getStartSessionTime));
        SatelliteFacilitySession bestSession = concurrentSessionList.get(0);
        Facility currentFacility = bestSession.getFacility();
        LocalDateTime startBusyTime = bestSession.getStartSessionTime();
        LocalDateTime endBusyTime = bestSession.getEndSessionTime();
        //удаляем из доступного расписания сеансы для конкретного примника в интервале действия текущего сеанса
        List<SatelliteFacilitySession> droppedSessions = freeFacilitySessionsMap.get(currentFacility).stream()
                .filter(session -> session.getStartSessionTime().isAfter(startBusyTime)
                        && session.getStartSessionTime().isBefore(endBusyTime))
                .toList();
        freeFacilitySessionsMap.get(currentFacility).removeAll(droppedSessions);
        //добавляем в итоговое расписание найденный сеанс спутник-приемник
        if (actualFacilitySessionsMap.containsKey(currentFacility)) {
            actualFacilitySessionsMap.get(currentFacility).add(bestSession);
        } else {
            actualFacilitySessionsMap.put(currentFacility, Stream.of(bestSession).toList());
        }
        return Optional.of(bestSession);
    }
}
