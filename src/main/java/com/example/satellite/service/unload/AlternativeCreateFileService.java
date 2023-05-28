package com.example.satellite.service.unload;

import com.example.satellite.config.SatelliteProperties;
import com.example.satellite.entity.Satellite;
import com.example.satellite.models.CalculatedCommunicationSession;
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
import java.util.stream.Collectors;

import static com.example.satellite.utils.ConstantUtils.REPORT_DIR;

/**
 * Сервис формирования расписания в альтернативной форме.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlternativeCreateFileService {

    /**
     * Настройки приложения.
     */
    private final SatelliteProperties properties;

    /**
     * Формирует расписание по спутникам в альтернативной форме.
     *
     * @param finishedScheduleMap Мапа с данными для составления расписания
     */
    public void createFile(Map<Satellite, List<CalculatedCommunicationSession>> finishedScheduleMap) {
        finishedScheduleMap.forEach((satellite, calculatedCommunicationSessions) -> {
            String fileName = String.format("%s.txt", satellite.getName());
            File scheduleFile = new File(properties.getAlternativeFormatDirectory(), fileName);

            StringBuilder comSessions = new StringBuilder();
            try (FileOutputStream fileOutputStream = new FileOutputStream(scheduleFile);
                 Writer fos = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                comSessions.append(String.format("%20s %30s %30s %20s %30s %30s\r\n\n", "Session With", "Start Session Time", "End Session Time", "Duration", "Change in memory per session", "Current memory"));
                String sessions = calculatedCommunicationSessions.stream()
                        .map(CalculatedCommunicationSession::toString)
                        .collect(Collectors.joining("\n"));
                comSessions.append(sessions);
                try {
                    fos.append(comSessions.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                fos.flush();
            } catch (Exception e) {
                FileUtils.deleteQuietly(scheduleFile);
                log.error(e.getMessage());
            }
        });

        //     File zip = new File(properties.getTempDirectory(), String.format("%s.zip", satellite.getName()));
        //      ZipParameters zipParameters = new ZipParameters();
        //       try {
//                ZipFile zipArchiver = new ZipFile(zip);
//                zipParameters.setFileNameInZip(fileName);
//                zipArchiver.addFile(scheduleFile);
//            } catch (Exception e) {
//                log.error(e.getMessage());
//            } finally {
//                FileUtils.deleteQuietly(scheduleFile);
//            }

    }

    /**
     * Генерирует миниотчет.
     *
     * @param report Информация.
     */
    public void report(String report) {

        String fileName = String.format("%s.txt", REPORT_DIR);
        File scheduleFile = new File(properties.getReportDirectory(), fileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(scheduleFile);
             Writer fos = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
            try {
                fos.append(report);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fos.flush();
        } catch (Exception e) {
            FileUtils.deleteQuietly(scheduleFile);
            log.error(e.getMessage());
        }
    }

}
