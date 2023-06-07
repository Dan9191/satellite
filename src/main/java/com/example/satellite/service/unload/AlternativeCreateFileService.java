package com.example.satellite.service.unload;

import com.example.satellite.entity.Satellite;
import com.example.satellite.models.CalculatedCommunicationSession;
import com.example.satellite.models.ReportsRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.satellite.utils.ConstantUtils.ALTERNATIVE_DIRECTORY;
import static com.example.satellite.utils.ConstantUtils.REPORT;

/**
 * Сервис формирования расписания в альтернативной форме.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlternativeCreateFileService {

    /**
     * Формирует расписание по спутникам в альтернативной форме.
     *
     * @param finishedScheduleMap Мапа с данными для составления расписания
     * @param zipArchiver         Архив с отчетами.
     */
    public void createFile(Map<Satellite, List<CalculatedCommunicationSession>> finishedScheduleMap, ZipFile zipArchiver) {
        finishedScheduleMap.forEach((satellite, calculatedCommunicationSessions) -> {
            try {
                String fileName = String.format("%s.txt", satellite.getName());
                StringBuilder comSessions = new StringBuilder();
                comSessions.append(String.format("%20s %30s %30s %20s %30s %30s %30s\r\n\n", "Session With",
                        "Start Session Time", "End Session Time", "Duration", "Change in memory per session",
                        "Current memory", "Data transferred"));
                String sessions = calculatedCommunicationSessions.stream()
                        .map(CalculatedCommunicationSession::toString)
                        .collect(Collectors.joining("\n"));
                comSessions.append(sessions);
                ZipParameters zipParameters = new ZipParameters();
                zipParameters.setFileNameInZip(ALTERNATIVE_DIRECTORY + File.separator + fileName);
                zipArchiver.addStream(new ByteArrayInputStream(comSessions.toString().getBytes(StandardCharsets.UTF_8)), zipParameters);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Генерирует миниотчет.
     *
     * @param reportsRows Информация.
     * @param zipArchiver Архив с отчетами.
     */
    public void report(List<ReportsRow> reportsRows, ZipFile zipArchiver) {
        try {
            String fileName = String.format("%s.txt", REPORT);
            StringBuilder comSessions = new StringBuilder();
            String report = reportsRows.stream()
                    .map(ReportsRow::toString)
                    .collect(Collectors.joining("\n"));
            comSessions.append(String.format("%30s %30s %30s \r\n\n", "Спутник",
                    "Дата переполнения", "Переданные данные за весь период"));
            comSessions.append(report);
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setFileNameInZip(fileName);
            zipArchiver.addStream(new ByteArrayInputStream(comSessions.toString().getBytes(StandardCharsets.UTF_8)), zipParameters);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
