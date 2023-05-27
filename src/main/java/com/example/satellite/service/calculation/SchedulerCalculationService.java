package com.example.satellite.service.calculation;

import com.example.satellite.entity.Area;
import com.example.satellite.entity.Facility;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.models.CalculatedCommunicationSession;
import com.example.satellite.repository.FacilityRepository;
import com.example.satellite.repository.SatelliteAreaSessionRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import com.example.satellite.repository.SatelliteRepository;
import com.example.satellite.service.unload.AlternativeCreateFileService;
import com.example.satellite.service.unload.AreaFileService;
import com.example.satellite.service.unload.FacilityFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
     * Сервис формирования расписания в альтернативной форме.
     */
    private final AlternativeCreateFileService alternativeCreateFileService;

    /**
     * Сервис для формирования файла выйгрузки расписания по траекториям.
     */
    private final AreaFileService areaFileService;

    /**
     * Сервис для формирования файла выйгрузки расписания по приемнику.
     */
    private final FacilityFileService facilityFileService;

    /**
     * Список доступных сеансов связи спутника и приемника земли.
     */
    private Map<Facility, List<SatelliteFacilitySession>> freeFacilitySessionsMap = new HashMap<>();

    /**
     * Список сеансов связи спутника и приемника земли, которые произойдут реально.
     */
    private Map<Facility, Map<Satellite, List<SatelliteFacilitySession>>> actualFacilitySessionsMap = new HashMap<>();


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
        Map<Satellite, List<SatelliteAreaSession>> satelliteAreaSessionsMap = findAllSatelliteAreaSessionsMap();
        log.info("End calculated area schedule {} ms", System.currentTimeMillis() - stageTime);
        stageTime = System.currentTimeMillis();
        log.info("Start calculated full schedule");
        memoryOverflowTime(satelliteAreaSessionsMap);
        log.info("End calculated memory Overflow Time {}", System.currentTimeMillis() - stageTime );

        areaFileService.createFile(satelliteAreaSessionsMap);
        facilityFileService.createFile(actualFacilitySessionsMap);
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
        Map<Satellite, List<CalculatedCommunicationSession>> finishedScheduleMap = new HashMap<>();
        satelliteAreaSessionsMap.forEach((satellite, sessionsList) -> {
            //итоговое расписание сеансов спутника
            List<CalculatedCommunicationSession> actualSatelliteSessions = new ArrayList<>();
            //возможно надо ввести дельту, которая будет увеличивать каждый день минимальный порог загроможденности памяти?
            Long memoryThreshold = 439804651110L;
            //текущая память спутника
            AtomicLong currentMemory = new AtomicLong(satellite.getSatelliteType().getTotalMemory());
            //скорость траты памяти при съемке
            long shootingMemorySpeed = satellite.getSatelliteType().getShootingSpeed();
            //скорость восполнения памяти при передаче
            long dataTransferSpeed = satellite.getSatelliteType().getDataTransferSpeed();
            //порядковый номер сеанса
            AtomicInteger orderNumber = new AtomicInteger(1);
            //конец предыдущего сеанса связи или съекмки
            LocalDateTime previousEndSession = LocalDateTime.MIN;

            for (int i = 0; i < sessionsList.size() - 1; i++) {
                //если память переполнилась, то завершаем формирование расписания
                if (currentMemory.get() <= 0L) {
                    break;
                }
                SatelliteAreaSession previousSession = sessionsList.get(i);
                SatelliteAreaSession nextSession = sessionsList.get(i + 1);
                LocalDateTime startFreeInterval = previousSession.getEndSessionTime();
                LocalDateTime endFreeInterval = nextSession.getStartSessionTime();
                //если конец предыдущего сеанса позже, чем начало текущего, то пропускаем итерацию цикла.
                //Возможно, если цикл передачи наложится на цикл съемки
                if (previousEndSession.isAfter(previousSession.getStartSessionTime())) {
                    continue;
                }
                long sessionMemorySpending = (long) previousSession.getDuration() * shootingMemorySpeed;
                currentMemory.addAndGet(-sessionMemorySpending);
                //производим вычисление первой сессии съемки земли
                actualSatelliteSessions.add(
                        new CalculatedCommunicationSession(
                                previousSession,
                                orderNumber.get(),
                                sessionMemorySpending,
                                currentMemory.get()
                        ));
                orderNumber.getAndIncrement();
                previousEndSession = previousSession.getEndSessionTime();
                //производим вычисление второй сессии передачи данных на землю (если получилось)
                Optional<SatelliteFacilitySession> satelliteFacilitySession =
                        findSatelliteFacilitySession(satellite, startFreeInterval, endFreeInterval);
                // если сессию передачи удалось найти - восстанавливаем объем текущей памяти
                if (satelliteFacilitySession.isPresent()) {
                    SatelliteFacilitySession session = satelliteFacilitySession.get();
                    long sessionMemoryIncome = (-1) * (long) session.getDuration() * dataTransferSpeed;
                    currentMemory.addAndGet(-sessionMemorySpending);
                    CalculatedCommunicationSession broadcastSession = new CalculatedCommunicationSession(
                            session,
                            orderNumber.get(),
                            sessionMemoryIncome,
                            currentMemory.get()
                    );
                    actualSatelliteSessions.add(broadcastSession);
                    orderNumber.getAndIncrement();
                    previousEndSession = session.getEndSessionTime();
                }
            }
//            System.out.println(satellite.getName());
//            System.out.println(previousEndSession);
//            System.out.println("---------");

            finishedScheduleMap.put(satellite, actualSatelliteSessions);
        });
        alternativeCreateFileService.createFile(finishedScheduleMap);
    }

    /**
     * Вычисление подходящего сеанса связи для спутника и земли. Найденный сеанс сохраняется в общую мапу расписаний.
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
        Map<Satellite, List<SatelliteFacilitySession>> satelliteFacilitySessionMap =
                Collections.singletonMap(satellite, Stream.of(bestSession).toList());
        if (actualFacilitySessionsMap.containsKey(currentFacility)) {
            Map<Satellite, List<SatelliteFacilitySession>> actualSessions =
                    actualFacilitySessionsMap.get(currentFacility);
            if (actualSessions.containsKey(satellite)) {
                actualSessions.get(satellite).add(bestSession);
            } else {
                List<SatelliteFacilitySession> bestSessionList = new ArrayList<>();
                bestSessionList.add(bestSession);
                actualSessions.put(satellite, bestSessionList);
            }
        } else {
            List<SatelliteFacilitySession> bestSessionList = new ArrayList<>();
            bestSessionList.add(bestSession);
            Map<Satellite, List<SatelliteFacilitySession>> newSatelliteSession = new HashMap<>();
            newSatelliteSession.put(satellite, bestSessionList);
            actualFacilitySessionsMap.put(currentFacility, newSatelliteSession);
        }
        return Optional.of(bestSession);
    }
}
