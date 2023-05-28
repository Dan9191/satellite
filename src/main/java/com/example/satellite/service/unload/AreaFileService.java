package com.example.satellite.service.unload;

import com.example.satellite.config.SatelliteProperties;
import com.example.satellite.entity.Area;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для формирования файла выйгрузки расписания по траекториям.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AreaFileService {

    /**
     * Настройки приложения.
     */
    private final SatelliteProperties properties;

    private long accessCounter = 0;

    /**
     * Тут нужно сформировать файл для сеансов траектории.
     *
     * @param satelliteAreaSessionsMap Данные для формирования файла.
     */
    public void createFile(Map<Satellite, List<SatelliteAreaSession>> satelliteAreaSessionsMap) {
        //подготовим мапу с данными для публикации.
        //данные ключа-area будут отдельным файлом
        Map<Area, Map<Satellite, List<SatelliteAreaSession>>> actualAreaSessionsMap = new HashMap<>();
        satelliteAreaSessionsMap.forEach((satellite, sessions) -> {
            Area area = satellite.getArea();
            if (actualAreaSessionsMap.containsKey(area)) {
                actualAreaSessionsMap.get(area).put(satellite, sessions);
            } else {
                Map<Satellite, List<SatelliteAreaSession>> map = new HashMap<>();
                map.put(satellite, sessions);
                actualAreaSessionsMap.put(area, map);
            }
        });


        //формируем файл
        actualAreaSessionsMap.forEach((area, satelliteSessionsMap) -> {
            String fileName = String.format("%s.txt", area.getName());
            File scheduleFile = new File(properties.getAreaDirectory(), fileName);

            try (FileOutputStream fileOutputStream = new FileOutputStream(scheduleFile);
                 Writer fos = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {

                StringBuilder sessions = new StringBuilder();
                Set<Satellite> satelliteSet = satelliteSessionsMap.keySet();
                for (Satellite satellite: satelliteSet) {
                    sessions.append("\n").append(satellite.getName()).append("\n\n");
                    sessions.append(String.format("Access %20s %30s %30s %20s\r\n\n", "Order Number", "Start Session Time", "End Session Time", "Duration"));
                    String sessionsRows = satelliteSessionsMap.get(satellite).stream()
                            .map(sas -> ++accessCounter + sas.toString())
                            .collect(Collectors.joining("\n"));
                    sessions.append(sessionsRows);
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
