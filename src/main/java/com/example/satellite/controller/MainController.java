package com.example.satellite.controller;

import com.example.satellite.service.UploadAreaFileService;
import com.example.satellite.service.UploadFacilityFileService;
import com.example.satellite.service.calculation.SchedulerCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.io.StringWriter;

@Controller
@RequestMapping("/v2/api")
@RequiredArgsConstructor
@Slf4j
public class MainController {

    /**
     * Сервис загрузки файлов.
     */
    private final UploadAreaFileService uploadAreaFileService;

    /**
     * Сервис загрузки файлов.
     */
    private final UploadFacilityFileService uploadFacilityFileService;

    @GetMapping
    public String blankConsole() {
        return "upload";
    }

    private final SchedulerCalculationService schedulerCalculationService;

    /**
     * Запуск вычисления расписания.
     */
    @GetMapping("/calculate-schedule")
    public String calculateSchedule(Model model) {
        long startTime = System.currentTimeMillis();
        schedulerCalculationService.findFasterSatellite();
        String message = String.format("Успешно вычислено за %s секунд", (System.currentTimeMillis() - startTime)/1000);
        model.addAttribute("message", message);
        return "upload";
    }

    /**
     * Метод для загрузки созвездий спутников.
     */
    @PostMapping("/area-upload")
    public String areaFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            uploadAreaFileService.readFile(file);
            model.addAttribute("message", "Файл " + file.getOriginalFilename() + " успешно загружен");
        } catch (Exception e) {
            model.addAttribute("warning", e.getMessage());
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                model.addAttribute("stackTrace", sw.toString());
            } catch (Exception ignored) {
                log.error("Can't write out exception", e);
            }
            return "upload";
        }
        return "upload";
    }

    /**
     * Метод для загрузки
     */
    @PostMapping("/facility-upload")
    public String facilityFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            uploadFacilityFileService.readFile(file);
            model.addAttribute("message", "Файл " + file.getOriginalFilename() + " успешно загружен");
        } catch (Exception e) {
            model.addAttribute("warning", e.getMessage());
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                model.addAttribute("stackTrace", sw.toString());
            } catch (Exception ignored) {
                log.error("Can't write out exception", e);
            }
            return "upload";
        }
        return "upload";
    }
}
