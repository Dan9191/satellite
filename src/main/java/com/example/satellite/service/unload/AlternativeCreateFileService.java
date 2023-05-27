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

            try (FileOutputStream fileOutputStream = new FileOutputStream(scheduleFile);
                 Writer fos = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                String sessions = calculatedCommunicationSessions.stream()
                        .map(CalculatedCommunicationSession::toString)
                        .collect(Collectors.joining("\n"));
                try {
                    fos.append(sessions);
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

}
