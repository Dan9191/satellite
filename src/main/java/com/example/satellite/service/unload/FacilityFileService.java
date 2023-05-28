package com.example.satellite.service.unload;

import com.example.satellite.config.SatelliteProperties;
import com.example.satellite.entity.Facility;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteFacilitySession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для формирования файла выйгрузки расписания по приемнику.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityFileService {

    /**
     * Настройки приложения.
     */
    private final SatelliteProperties properties;

    private long accessCounter = 0;

    /**
     * Сформировать файл для сеансов передачи информации.
     *
     * @param actualFacilitySessionsMap Данные для формирования файла.
     */
    public void createFile(Map<Facility, Map<Satellite, List<SatelliteFacilitySession>>> actualFacilitySessionsMap) {
        actualFacilitySessionsMap.forEach((facility, satelliteSessionsMap) -> {
            String fileName = String.format("%s.txt", facility.getName());
            File scheduleFile = new File(properties.getFacilityDirectory(), fileName);

            try (FileOutputStream fileOutputStream = new FileOutputStream(scheduleFile);
                 Writer fos = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {

                StringBuilder sessions = new StringBuilder();
                Set<Satellite> satelliteSet = satelliteSessionsMap.keySet();
                for (Satellite satellite: satelliteSet) {
                    sessions.append("\n").append(satellite.getName()).append("\n\n");
                    sessions.append(String.format("Access%18s %30s %30s %20s\r\n\n", "Order Number", "Start Session Time", "End Session Time", "Duration"));
                    String sessionsRows = satelliteSessionsMap.get(satellite).stream()
                            .map(sfs -> ++accessCounter +
                                    String.format("%20s", satelliteSessionsMap.get(satellite).indexOf(sfs)+1) +
                                    sfs.toString())
                            .collect(Collectors.joining("\n"));
                    sessions.append(sessionsRows).append("\n\n");
                }
                try {
                    fos.append(sessions.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                fos.flush();
            } catch (Exception e) {
                FileUtils.deleteQuietly(scheduleFile);
                log.error(e.getMessage());
            }
        });
    }
}
