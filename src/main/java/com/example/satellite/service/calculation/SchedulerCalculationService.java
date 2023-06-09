package com.example.satellite.service.calculation;

import com.example.satellite.entity.Facility;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.models.CalculatedCommunicationSession;
import com.example.satellite.models.ReportsRow;
import com.example.satellite.repository.FacilityRepository;
import com.example.satellite.repository.SatelliteAreaSessionRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import com.example.satellite.repository.SatelliteRepository;
import com.example.satellite.service.unload.AlternativeCreateFileService;
import com.example.satellite.service.unload.AreaFileService;
import com.example.satellite.service.unload.FacilityFileService;
import com.example.satellite.utils.HttpFileUtils;
import com.example.satellite.utils.MemoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.satellite.utils.ConstantUtils.IS_SENDING_TIME;
import static com.example.satellite.utils.ConstantUtils.IS_SHOOTING_TIME;
import static com.example.satellite.utils.ConstantUtils.MAIN_DIRECTORY;

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
     * Набросок метода для вычисления времени конца памяти у каждого из спутников.
     */
    public ResponseEntity<InputStreamResource> calculateSchedule() throws IOException {
        //Список возможных сеансов связи спутника и приемника земли.
        Map<Facility, List<SatelliteFacilitySession>> freeFacilitySessionsMap = findAllFacilitySessionsMap();
        //Список сеансов связи спутника и приемника земли, которые произойдут реально.
        Map<Facility, Map<Satellite, List<SatelliteFacilitySession>>> actualFacilitySessionsMap = new HashMap<>();
        FileUtils.deleteQuietly(new File(String.format("%s.zip", MAIN_DIRECTORY)));
        long startTime = System.currentTimeMillis();
        log.info("Start process {}", LocalDateTime.now());
        log.info("Start calculated facility schedule ");
        long stageTime = System.currentTimeMillis();
        log.info("End calculated facility schedule {} ms", stageTime - startTime);

        log.info("Start calculated area schedule");
        Map<Satellite, List<SatelliteAreaSession>> satelliteAreaSessionsMap = findAllSatelliteAreaSessionsMap();
        log.info("End calculated area schedule {} ms", System.currentTimeMillis() - stageTime);
        stageTime = System.currentTimeMillis();
        log.info("Start calculated full schedule");

        Path mainDir = Files.createDirectories(Paths.get(MAIN_DIRECTORY));
        File zip = new File(String.format("%s.zip", MAIN_DIRECTORY));
        ZipFile zipArchiver = new ZipFile(zip);
        zipArchiver.close();
        memoryOverflowTime(satelliteAreaSessionsMap, freeFacilitySessionsMap, actualFacilitySessionsMap, zipArchiver);

        areaFileService.createFile(satelliteAreaSessionsMap, zipArchiver);
        facilityFileService.createFile(actualFacilitySessionsMap, zipArchiver);
        FileUtils.deleteQuietly(mainDir.toFile());
        log.info("End calculated memory Overflow Time {}", System.currentTimeMillis() - stageTime );
        return HttpFileUtils.uploadFile(zipArchiver.getFile(), "calculatedSchedule.zip");
    }

    /**
     * Собирает данные о всех сеансах связи спутника с наземным приемником.
     *
     * @return Мапа сеансов связи спутника с землей, где key - наземный приемник, value - список его сеансов.
     */
    private synchronized Map<Facility, List<SatelliteFacilitySession>> findAllFacilitySessionsMap() {
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
    private synchronized Map<Satellite, List<SatelliteAreaSession>> findAllSatelliteAreaSessionsMap() {
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
     * @param satelliteAreaSessionsMap  Мапа сеансов съемки спутника, где key - спутник, value - сеансы съемки.
     * @param freeFacilitySessionsMap   Список возможных сеансов связи спутника и приемника земли.
     * @param actualFacilitySessionsMap Список сеансов связи спутника и приемника земли, которые произойдут реально.
     * @param zipArchiver               Архив с отчетами.
     */
    private void memoryOverflowTime(Map<Satellite, List<SatelliteAreaSession>> satelliteAreaSessionsMap,
                                    Map<Facility, List<SatelliteFacilitySession>> freeFacilitySessionsMap,
                                    Map<Facility, Map<Satellite, List<SatelliteFacilitySession>>> actualFacilitySessionsMap,
                                    ZipFile zipArchiver) {
        Map<Satellite, List<CalculatedCommunicationSession>> finishedScheduleMap = new HashMap<>();
        List<ReportsRow> reportsRows = new ArrayList<>();
        satelliteAreaSessionsMap.forEach((satellite, sessionsList) -> {
            //итоговое расписание сеансов спутника
            List<CalculatedCommunicationSession> actualSatelliteSessions = new ArrayList<>();
            //возможно надо ввести дельту, которая будет увеличивать каждый день минимальный порог загроможденности памяти?
            //  Long memoryThreshold = 439804651110L;
            //текущая память спутника
            long currentMemory = satellite.getSatelliteType().getTotalMemory();
            final long TOTAL_MEMORY = satellite.getSatelliteType().getTotalMemory();
            //скорость траты памяти при съемке
            long shootingMemorySpeed = satellite.getSatelliteType().getShootingSpeed();
            //скорость восполнения памяти при передаче
            long dataTransferSpeed = satellite.getSatelliteType().getDataTransferSpeed();
            //суммарный объем переданной информации
            long memorySendingSum = 0L;
            //порядковый номер сеанса
            AtomicInteger orderNumber = new AtomicInteger(1);
            //конец предыдущего сеанса связи или съемки
            LocalDateTime previousEndSession = LocalDateTime.MIN;

            //дата первого переполнения памяти
            Optional<LocalDateTime> memoryOverflowData = Optional.empty();

            for (int i = 0; i < sessionsList.size() - 1; i++) {
                SatelliteAreaSession previousSession = sessionsList.get(i);
                SatelliteAreaSession nextSession = sessionsList.get(i + 1);
                LocalDateTime startFreeInterval = previousSession.getEndSessionTime();
                LocalDateTime endFreeInterval = nextSession.getStartSessionTime();
                //если конец предыдущего сеанса позже, чем начало текущего, то пропускаем итерацию цикла.
                //это возможно, если цикл передачи наложится на цикл съемки
                if (previousEndSession.isAfter(previousSession.getStartSessionTime())) {
                    continue;
                }
                //количество отснятой информации
                long sessionMemorySpending = (long) previousSession.getDuration() * shootingMemorySpeed;
                currentMemory = MemoryUtils.memorySubtraction(currentMemory, sessionMemorySpending);
                //производим вычисление первой сессии съемки земли
                actualSatelliteSessions.add(
                        new CalculatedCommunicationSession(
                                previousSession,
                                orderNumber.get(),
                                sessionMemorySpending,
                                currentMemory,
                                MemoryUtils.readableSize(memorySendingSum)
                        ));
                orderNumber.getAndIncrement();
                previousEndSession = previousSession.getEndSessionTime();

                //если произошло первое опустошение памяти - сохраняем такую дату
                if (currentMemory == 0L && memoryOverflowData.isEmpty()) {
                    memoryOverflowData = Optional.of(endFreeInterval);
                }

                //производим вычисление второй сессии передачи данных на землю (если получилось)
                Optional<SatelliteFacilitySession> satelliteFacilitySession =
                        findSatelliteFacilitySession(satellite,
                                startFreeInterval,
                                endFreeInterval,
                                freeFacilitySessionsMap,
                                actualFacilitySessionsMap);
                // если сессию передачи удалось найти - восстанавливаем объем текущей памяти
                if (satelliteFacilitySession.isPresent()) {
                    SatelliteFacilitySession session = satelliteFacilitySession.get();
                    long sessionMemoryIncome = (long) session.getDuration() * dataTransferSpeed;
                    long memoryAfterSending = MemoryUtils.memorySum(currentMemory, sessionMemoryIncome, TOTAL_MEMORY);
                    long realSendingDelta = MemoryUtils.actuallySendingMemory(sessionMemoryIncome, currentMemory, memoryAfterSending);
                    memorySendingSum += realSendingDelta;
                    currentMemory = memoryAfterSending;
                    CalculatedCommunicationSession broadcastSession = new CalculatedCommunicationSession(
                            session,
                            orderNumber.get(),
                            realSendingDelta,
                            currentMemory,
                            MemoryUtils.readableSize(memorySendingSum)
                    );
                    actualSatelliteSessions.add(broadcastSession);
                    orderNumber.getAndIncrement();
                    previousEndSession = session.getEndSessionTime();
                    session.setDataMb(MemoryUtils.readableMbSize(realSendingDelta));
                }

                // если темное время суток, память не до конца свободна и пока удается найти сессию выгрузки данных-будем выгружать
                while (currentMemory <= TOTAL_MEMORY - 100
                        && IS_SENDING_TIME.test(previousEndSession)) {
                    satelliteFacilitySession =
                            findSatelliteFacilitySession(satellite,
                                    previousEndSession,
                                    endFreeInterval,
                                    freeFacilitySessionsMap,
                                    actualFacilitySessionsMap);
                    if (satelliteFacilitySession.isEmpty()) {
                        break;
                    }
                    SatelliteFacilitySession session = satelliteFacilitySession.get();
                    long sessionMemoryIncome = (long) session.getDuration() * dataTransferSpeed;
                    long memoryAfterSending = MemoryUtils.memorySum(currentMemory, sessionMemoryIncome, TOTAL_MEMORY);
                    long realSendingDelta = MemoryUtils.actuallySendingMemory(sessionMemoryIncome, currentMemory, memoryAfterSending);
                    memorySendingSum += realSendingDelta;
                    currentMemory = memoryAfterSending;
                    CalculatedCommunicationSession broadcastSession = new CalculatedCommunicationSession(
                            session,
                            orderNumber.get(),
                            realSendingDelta,
                            currentMemory,
                            MemoryUtils.readableSize(memorySendingSum)
                    );
                    actualSatelliteSessions.add(broadcastSession);
                    orderNumber.getAndIncrement();
                    previousEndSession = session.getEndSessionTime();
                    session.setDataMb(MemoryUtils.readableMbSize(realSendingDelta));
                }
            }
            //копим информацию для миниотчета
            reportsRows.add(new ReportsRow(satellite.getName(), memoryOverflowData, MemoryUtils.readableSize(memorySendingSum)));
            finishedScheduleMap.put(satellite, actualSatelliteSessions);
        });
       alternativeCreateFileService.createFile(finishedScheduleMap, zipArchiver);
       alternativeCreateFileService.report(reportsRows, zipArchiver);
    }

    /**
     * Вычисление подходящего сеанса связи для спутника и земли. Найденный сеанс сохраняется в общую мапу расписаний.
     *
     * @param satellite                 Спутник, для которого ищем расписание.
     * @param startFreeInterval         Начала интервала, в который спутник доступен для передачи данных.
     * @param endFreeInterval           Конец интервала, в который спутник доступен для передачи данных.
     * @param freeFacilitySessionsMap   Список возможных сеансов связи спутника и приемника земли.
     * @param actualFacilitySessionsMap Список сеансов связи спутника и приемника земли, которые произойдут реально.
     * @return Сеанс связи спутника и земли
     */
    private synchronized Optional<SatelliteFacilitySession> findSatelliteFacilitySession(Satellite satellite,
                                                                                         LocalDateTime startFreeInterval,
                                                                                         LocalDateTime endFreeInterval,
                                                                                         Map<Facility, List<SatelliteFacilitySession>> freeFacilitySessionsMap,
                                                                                         Map<Facility, Map<Satellite, List<SatelliteFacilitySession>>> actualFacilitySessionsMap) {
        //поиск подходящих сеансов
        Optional<SatelliteFacilitySession> bestSessionOpt = findBestSession(
                satellite,
                startFreeInterval,
                endFreeInterval,
                freeFacilitySessionsMap
        );

        if (bestSessionOpt.isEmpty()) {
            return bestSessionOpt;
        }
        //получаем данные самого подходящего сеанса
        SatelliteFacilitySession bestSession = bestSessionOpt.get();
        Facility currentFacility = bestSession.getFacility();
        LocalDateTime startBusyTime = bestSession.getStartSessionTime();
        LocalDateTime endBusyTime = bestSession.getEndSessionTime();
        //удаляем из доступного расписания сеансы для конкретного наземного приемника в интервале действия найденного сеанса
        List<SatelliteFacilitySession> droppedSessions = freeFacilitySessionsMap.get(currentFacility).stream()
                .filter(session -> session.getStartSessionTime().isAfter(startBusyTime)
                        && session.getStartSessionTime().isBefore(endBusyTime))
                .toList();
        freeFacilitySessionsMap.get(currentFacility).removeAll(droppedSessions);
        //добавляем в итоговое расписание найденный сеанс спутник-приемник
        saveSendSession(bestSession, actualFacilitySessionsMap);
        return Optional.of(bestSession);
    }

    /**
     * Поиск наилучшего сеанса передачи данных
     *
     * @param satellite               Спутник, для которого ищем расписание.
     * @param startFreeInterval       Начала интервала, в который спутник доступен для передачи данных.
     * @param endFreeInterval         Конец интервала, в который спутник доступен для передачи данных.
     * @param freeFacilitySessionsMap Список возможных сеансов связи спутника и приемника земли.
     * @return результат поиска.
     */
    private Optional<SatelliteFacilitySession> findBestSession(Satellite satellite,
                                                               LocalDateTime startFreeInterval,
                                                               LocalDateTime endFreeInterval,
                                                               Map<Facility, List<SatelliteFacilitySession>> freeFacilitySessionsMap) {
        Optional<SatelliteFacilitySession> bestSessionOpt = Optional.empty();
        for (Facility facility: freeFacilitySessionsMap.keySet()) {
            Optional<SatelliteFacilitySession> optSession = freeFacilitySessionsMap.get(facility).stream()
                    .filter(session -> session.getSatellite().equals(satellite))
                    .filter(session -> session.getStartSessionTime().isAfter(startFreeInterval))
                    .filter(session -> session.getStartSessionTime().isBefore(endFreeInterval))
                    .findFirst();
            if (optSession.isPresent()) {
                bestSessionOpt = bestSessionOpt.isEmpty() ?
                        optSession : Optional.of(getBetterSession(bestSessionOpt.get(), optSession.get()));
            }
        }
        return bestSessionOpt;
    }

    /**
     * Сохраняем найденную сессию передачи данных в итоговое расписание.
     *
     * @param bestSession               Сеанс передачи данных.
     * @param actualFacilitySessionsMap Список сеансов связи спутника и приемника земли, которые произойдут реально.
     */
    private void saveSendSession(SatelliteFacilitySession bestSession,
                                 Map<Facility, Map<Satellite, List<SatelliteFacilitySession>>> actualFacilitySessionsMap) {
        Satellite satellite = bestSession.getSatellite();
        Facility facility = bestSession.getFacility();
        if (actualFacilitySessionsMap.containsKey(facility)) {
            Map<Satellite, List<SatelliteFacilitySession>> actualSessions =
                    actualFacilitySessionsMap.get(facility);
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
            actualFacilitySessionsMap.put(facility, newSatelliteSession);
        }
    }


    /**
     * Возврат сессии передачи данных, которая началась раньше.
     *
     * @param ses1 Первая сессия.
     * @param ses2 Вторая сессия.
     * @return результат.
     */
    public SatelliteFacilitySession getBetterSession(SatelliteFacilitySession ses1, SatelliteFacilitySession ses2) {
        LocalDateTime ses1StartSession = ses1.getStartSessionTime();
        LocalDateTime ses2StartSession = ses2.getStartSessionTime();
        return ses1StartSession.isBefore(ses2StartSession) ? ses1 : ses2;
    }
}
