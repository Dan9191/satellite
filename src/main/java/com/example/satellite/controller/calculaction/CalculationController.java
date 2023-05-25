package com.example.satellite.controller.calculaction;

import com.example.satellite.repository.SatelliteRepository;
import com.example.satellite.service.calculation.SchedulerCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/v1/schedule")
@RequiredArgsConstructor
@Slf4j
public class CalculationController {

    private final SchedulerCalculationService schedulerCalculationService;

    @GetMapping("/faster-satellite")
    public ResponseEntity test() {
        schedulerCalculationService.findFasterSatellite();
        return ResponseEntity.ok().body("asdasd");
    }
}
