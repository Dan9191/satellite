package com.example.satellite.service.unload;

import com.example.satellite.entity.Facility;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteFacilitySession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.example.satellite.utils.ConstantUtils.FACILITY_DIRECTORY;

/**
 * Сервис для формирования файла выгрузки расписания по приемнику.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityFileService {

    /**
     * Сформировать файл для сеансов передачи информации.
     *
     * @param actualFacilitySessionsMap Данные для формирования файла.
     * @param zipArchiver               Архив с отчетами.
     * @return Папка с расписанием по сеансам передачи информации на назмемный приемник.
     */
    public void createFile(Map<Facility, Map<Satellite, List<SatelliteFacilitySession>>> actualFacilitySessionsMap, ZipFile zipArchiver) {
        Map<Facility, List<SatelliteFacilitySession>> facilityMap = new HashMap<>();
        actualFacilitySessionsMap.forEach((facility, satelliteSessionsMap) -> {
            List<SatelliteFacilitySession> sessions = new ArrayList<>();
            satelliteSessionsMap.forEach(((satellite, satelliteSessions) -> {
                sessions.addAll(satelliteSessions);
            }));
            sessions.sort(Comparator.comparing(SatelliteFacilitySession::getStartSessionTime));
            facilityMap.put(facility, sessions);
        });

        facilityMap.forEach((facility, sessionsList) -> {
            try {
                String fileName = String.format("Ground_%s.txt", facility.getName());
                AtomicInteger orderNumb = new AtomicInteger();
                StringBuilder stringBuilder = new StringBuilder();
                AtomicLong totalDataTransferredPerPeriod = new AtomicLong(0L);
                stringBuilder.append(facility.getName()).append(" - имя станции\r\n\n");
                stringBuilder.append("-------------------------\r\n\n");
                stringBuilder.append(String.format("Access %20s %30s %20s %20s %20s\r\n\n",
                        "Start Session Time", "End Session Time", "Duration", "Sat name", "Data mb"));
                String sessionsRows = sessionsList.stream()
                        .map(sfs -> {
                            String row = orderNumb.incrementAndGet() + "    " + sfs.toString();
                            if (isTheLastSessionOfTheDay(sessionsList, sessionsList.indexOf(sfs))){
                                Long transferredPerDay = calculateTotalDataTransferredPerDay(sessionsList, sfs.getStartSessionTime());
                                row += "\n\nTotal data transferred per day: " + transferredPerDay + " MB\n";
                                totalDataTransferredPerPeriod.addAndGet(transferredPerDay);
                            }
                            return row;
                        })
                        .collect(Collectors.joining("\n"));
                stringBuilder.append(sessionsRows).append("\n\n");
                stringBuilder.append("Total data transferred per period: " + totalDataTransferredPerPeriod.get() + " MB\n");
                ZipParameters zipParameters = new ZipParameters();
                zipParameters.setFileNameInZip(FACILITY_DIRECTORY + File.separator + fileName);
                zipArchiver.addStream(new ByteArrayInputStream(stringBuilder.toString().getBytes(StandardCharsets.UTF_8)), zipParameters);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } );
    }

    private boolean isTheLastSessionOfTheDay(List <SatelliteFacilitySession> facilitySessions, int index){
        return index == facilitySessions.size() - 1 ||
                !facilitySessions.get(index).getStartSessionTime().toLocalDate()
                        .equals(facilitySessions.get(index + 1).getStartSessionTime().toLocalDate());
    }

    private Long calculateTotalDataTransferredPerDay(List<SatelliteFacilitySession> facilitySessions,
                                                     LocalDateTime date){
        return facilitySessions.stream()
                .filter(sfs -> sfs.getEndSessionTime().toLocalDate().equals(date.toLocalDate()))
                .filter(sfs -> sfs.getDataMb() != null)
                .mapToLong(sfs -> Long.parseLong(sfs.getDataMb().substring(0, sfs.getDataMb().length() - 3)))
                .sum();
    }
}
