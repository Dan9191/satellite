package com.example.satellite.service.unload;

import com.example.satellite.entity.Area;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.example.satellite.utils.ConstantUtils.AREA_DIRECTORY;

/**
 * Сервис для формирования файла выйгрузки расписания по траекториям.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AreaFileService {

    /**
     * Тут нужно сформировать файл для сеансов траектории.
     *
     * @param satelliteAreaSessionsMap Данные для формирования файла.
     * @param zipArchiver              Архив с отчетами.
     */
    public void createFile(Map<Satellite, List<SatelliteAreaSession>> satelliteAreaSessionsMap, ZipFile zipArchiver) {
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

        AtomicLong accessCounter = new AtomicLong();
        //формируем файл
        actualAreaSessionsMap.forEach((area, satelliteSessionsMap) -> {
            try {
                String fileName = String.format("%s.txt", area.getName());
                StringBuilder sessions = new StringBuilder();
                Set<Satellite> satelliteSet = satelliteSessionsMap.keySet();
                for (Satellite satellite: satelliteSet) {
                    sessions.append("\n").append(satellite.getName()).append("\n\n");
                    sessions.append(String.format("Access%18s %30s %30s %20s\r\n\n", "Order Number", "Start Session Time", "End Session Time", "Duration"));
                    String sessionsRows = satelliteSessionsMap.get(satellite).stream()
                            .map(sas -> accessCounter.incrementAndGet() +
                                    String.format("%20s", satelliteSessionsMap.get(satellite).indexOf(sas)+1) +
                                    sas.toString())
                            .collect(Collectors.joining("\n"));
                    sessions.append(sessionsRows).append("\n\n");
                }
                ZipParameters zipParameters = new ZipParameters();
                zipParameters.setFileNameInZip(AREA_DIRECTORY + File.separator + fileName);
                zipArchiver.addStream(new ByteArrayInputStream(sessions.toString().getBytes(StandardCharsets.UTF_8)), zipParameters);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
