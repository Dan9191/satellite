package com.example.satellite.service.unload;

import com.example.satellite.entity.Facility;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteFacilitySession;
import com.example.satellite.repository.FacilityRepository;
import com.example.satellite.repository.SatelliteFacilitySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для формирования файла выйгрузки расписания по приемнику.
 */
@Service
@RequiredArgsConstructor
public class UnloadFacilityFileService {

    /**
     * Репозиторий для работы с приемником.
     */
    private final FacilityRepository facilityRepository;

    /**
     * Репозиторий для работы с сеансами съемки спутника.
     */
    private final SatelliteFacilitySessionRepository satelliteFacilitySessionRepository;

    /**
     * Тут нужно сформировать файл для сеансов передачи информации.
     *
     * @param facilityName Название траектории.
     */
    public void unloadFile(String facilityName) {
        Optional<Facility> facility = facilityRepository.findByName(facilityName);

        //мапа с тестовыми данными
        Map<Satellite, List<SatelliteFacilitySession>> sessionsMap = new HashMap<>();
        if (facility.isPresent()) {
            sessionsMap = satelliteFacilitySessionRepository.findByFacilityOrderByStartSessionTime(facility.get()).stream()
                    .limit(200)
                    .collect(Collectors.groupingBy(SatelliteFacilitySession::getSatellite));
        }

        System.out.println("asdasdasd");
    }
}
