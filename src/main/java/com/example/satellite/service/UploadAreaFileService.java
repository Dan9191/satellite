package com.example.satellite.service;

import com.example.satellite.entity.*;
import com.example.satellite.models.CommunicationSession;
import com.example.satellite.repository.*;
import com.example.satellite.utils.ValidateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.example.satellite.utils.ConstantUtils.DATE_TIME_FORMATTER;
import static com.example.satellite.utils.ConstantUtils.END_SATELLITE_PATTERN;
import static com.example.satellite.utils.ConstantUtils.SATELLITE_NAME_PREFIX;
import static com.example.satellite.utils.ConstantUtils.SESSION_MATCHES_SIGN;

/**
 * Сервис загрузки расписаний для группировки спутников, принадлежащих одной траектории.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UploadAreaFileService {

    private final AreaRepository areaRepository;

    /**
     * Репозиторий для работы со спутником.
     */
    private final SatelliteRepository satelliteRepository;

    private final SatelliteTypeRepository satelliteTypeRepository;

    /**
     * Репозиторий для работы с сеансами съемки спутника.
     */
    private final SatelliteAreaSessionRepository satelliteAreaSessionRepository;

    private final UploadedFilesRepository uploadedFilesRepository;

    /**
     * Извлечение содержимого файла и сохранение расписаний.
     *
     * @param file Загружаемый файл.
     * @throws IOException Файл уже был прочитан.
     */
    public void readFile(MultipartFile file) throws IOException {
        String baseFileName = FilenameUtils.getBaseName(file.getOriginalFilename());
        if (file.isEmpty()) {
            log.error("file is empty.");
            throw new IOException("Треубется выбрать файл для загрузки");
        }
        //проверка начилия файла в БД
        if (uploadedFilesRepository.findByName(baseFileName).isPresent()) {
            log.error("file '{}' has already been loaded into the database.", baseFileName);
            throw new IOException(String.format("Файл '%s' уже загружен в базу данных.", baseFileName));
        }
        List<String> allRows = new BufferedReader(new InputStreamReader(file.getInputStream())).lines().toList();
        Map<String, List<CommunicationSession>> sessionMap = parseFile(allRows);

        sessionMap.forEach((key, value) -> {
            Satellite satellite;
            String areaName = key.substring(10, 12);
            Area area = areaRepository.findByName(areaName).orElse(new Area(areaName));
            areaRepository.save(area);
            if (satelliteRepository.findFirstByName(key).isEmpty()) {
                satellite = new Satellite();
                boolean kinosatSign = ValidateUtils.kinosatValidName(areaName);
                boolean zorkiySign = ValidateUtils.zorkiyValidName(areaName);
                if (kinosatSign && !zorkiySign) {
                    satellite.setSatelliteType(satelliteTypeRepository.getReferenceById(1));
                } else if (!kinosatSign && zorkiySign) {
                    satellite.setSatelliteType(satelliteTypeRepository.getReferenceById(2));
                } else {
                    log.error("unknown satellite type '{}'", key);
                }
                satellite.setName(key);
                satellite.setArea(area);
                satelliteRepository.save(satellite);
            } else {
                satellite = satelliteRepository.findFirstByName(key).get();
                if (satellite.getArea() == null) {
                    satellite.setArea(area);
                    satelliteRepository.save(satellite);
                }
            }
            List<SatelliteAreaSession> sessionList = value.stream()
                    .map(sessionData -> new SatelliteAreaSession(satellite, area, sessionData))
                    .toList();
            satelliteAreaSessionRepository.saveBatch(sessionList);
        });
        uploadedFilesRepository.save(new UploadedFile(baseFileName));
        log.info("file '{}' successfully read", baseFileName);
    }


    /**
     * Извлечение содержимого файла и группировка расписаний по спутникам.
     *
     * @param allRows Содержимое файла
     * @return Расписание сеансов с каждым из спутников
     */
    private Map<String, List<CommunicationSession>> parseFile(List<String> allRows) {
        Map<String, List<CommunicationSession>> communicationMap = new HashMap<>();
        List<String> satelliteList = getSatelliteNames(allRows);
        satelliteList.forEach(name -> communicationMap.put(name, calculateSessionList(allRows, name)));
        return communicationMap;
    }

    /**
     * Поулчаем список спутников.
     *
     * @param allRows      Содержимое файла.* @return Список спутников, которые используются в файле.
     */
    private List<String> getSatelliteNames(List<String> allRows) {
        String startSatellitesPattern = "AreaTarget-Russia-To-";
        String satelliteNames = "";
        boolean startSatelliteRowsIndex = false;
        boolean endSatelliteRowsIndex = false;
        for (String row : allRows) {
            if (!startSatelliteRowsIndex) {
                if (row.startsWith(startSatellitesPattern)) {
                    startSatelliteRowsIndex = true;
                    satelliteNames = row;
                }
                if (row.endsWith(END_SATELLITE_PATTERN)) {
                    break;
                }
            } else if (!endSatelliteRowsIndex) {
                satelliteNames += row;
                if (row.endsWith(END_SATELLITE_PATTERN)) {
                    break;
                }
            } else {
                break;
            }
        }
        String satelliteBillet = satelliteNames.replaceAll(startSatellitesPattern, "")
                .replaceAll(END_SATELLITE_PATTERN, "")
                .replaceAll(SATELLITE_NAME_PREFIX, "")
                .replaceAll("\\s", "");
        return Arrays.stream(satelliteBillet.split(",")).toList();
    }

    /**
     * Вычисление сеансов обмена данными для выбранного спутника.
     *
     * @param allRows      Содержимое файла.
     * @param name         Название спутника.
     * @return Сеансы обмена данными между указанными спутником и приемником.
     */
    private List<CommunicationSession> calculateSessionList(List<String> allRows, String name) {
        String areaName = "Russia-To-";
        String fullName = areaName + name;
        List<CommunicationSession> satelliteSessions = new ArrayList<>();
        boolean findSatellite = false;
        boolean startSchedule = false;
        boolean endSchedule = false;
        for (String row : allRows) {
            Matcher m = SESSION_MATCHES_SIGN.matcher(row);
            if (!findSatellite) {
                if (row.equals(fullName)) {
                    findSatellite = true;
                }
            } else if (!startSchedule) {
                if (m.matches()) {
                    startSchedule = true;
                    satelliteSessions.add(createCommunicationSession(row));
                }
            } else if (!endSchedule) {
                if (m.matches()) {
                    satelliteSessions.add(createCommunicationSession(row));
                } else {
                    break;
                }
            }
        }
        return satelliteSessions;
    }

    /**
     * Десериализация строки в объект сеанса.
     *
     * @param row Строка, содержащая время и продолжительность сеанса
     * @return Объект сеанса.
     */
    private CommunicationSession createCommunicationSession(String row) {
        String preparedString = row.trim().replaceAll("\\s{2,}", "-----");
        List<String> prepareAttr = Arrays.stream(preparedString.split("-----")).toList();
        Integer number = Integer.parseInt(prepareAttr.get(0));
        LocalDateTime startSessionTime = LocalDateTime.parse(prepareAttr.get(1), DATE_TIME_FORMATTER);
        LocalDateTime endSessionTime = LocalDateTime.parse(prepareAttr.get(2), DATE_TIME_FORMATTER);
        float duration = Float.parseFloat(prepareAttr.get(3));
        CommunicationSession communicationSession = new CommunicationSession();
        communicationSession.setNumber(number);
        communicationSession.setStartSessionTime(startSessionTime);
        communicationSession.setEndSessionTime(endSessionTime);
        communicationSession.setDuration(duration);
        return communicationSession;
    }

}
