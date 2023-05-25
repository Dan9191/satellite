package com.example.satellite.service;

import com.example.satellite.entity.SatelliteFacilitySession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityScheduleSavingService {

    private final SatelliteMemoryObservanceService memoryObservanceService;
    private long accessCounter = 0;

    public void saveSchedule(List<SatelliteFacilitySession>facilitySessions, File file){
       try {
           BufferedWriter writer = new BufferedWriter(new FileWriter(file));
           writer.write(getHeader(facilitySessions.get(0).getFacility().getName()));
           //needs data transferred per session information
           facilitySessions.stream()
                   .filter(sfs -> sfs.getSatellite()!=null)
                   .forEach(sfs -> {
               try {
                   writer.write(++accessCounter + getContent(sfs));
                   writer.flush();
               } catch (IOException e) {
                   log.error("Возникла ошибка во время записи в файл.");
               }
           });
           System.out.println("Запись завершена.");
           writer.close();
       } catch (IOException ex){
           log.error("Возникла ошибка во время записи в файл.");
       }

    }

    private String getHeader(String facility){
        return facility + "\nAccess \tStartTime (UTCG) \t\t StopTime (UTCG) \t\t" +
                "Duration (sec) \t\t Sat name \t\t Data (Mbytes) \n";
    }

    private String getContent(SatelliteFacilitySession session){
        return "\t\t" + session.getStartSessionTime() + "\t" +
                session.getEndSessionTime() + "\t" + session.getDuration() +
                "\t\t" + session.getSatellite().getName() + "\t\t" +
                memoryObservanceService.getTransferredDataVolume(session) +"\n";
    }
}
