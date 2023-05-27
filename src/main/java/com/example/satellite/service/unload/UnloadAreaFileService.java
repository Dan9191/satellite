package com.example.satellite.service.unload;

import com.example.satellite.entity.Area;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.repository.AreaRepository;
import com.example.satellite.repository.SatelliteAreaSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для формирования файла выйгрузки расписания по траекториям.
 */
@Service
@RequiredArgsConstructor
public class UnloadAreaFileService {

    /**
     * Репозиторий для работы со спутником.
     */
    private final AreaRepository areaRepository;

    /**
     * Репозиторий для работы с сеансами съемки спутника.
     */
    private final SatelliteAreaSessionRepository satelliteAreaSessionRepository;

    /**
     * Тут нужно сформировать файл для сеансов траектории.
     *
     * @param areaName Название траектории.
     */
    public void unloadFile(String areaName) {
        Optional<Area> area = areaRepository.findByName(areaName);
        Map<Satellite, List<SatelliteAreaSession>> sessionsMap = new HashMap<>();
        if (area.isPresent()) {
            sessionsMap = satelliteAreaSessionRepository.findAllByAreaOrderByStartSessionTime(area.get()).stream()
                    .limit(200)
                    .collect(Collectors.groupingBy(SatelliteAreaSession::getSatellite));
        }

        System.out.println("asdasdasd");
    }
}
