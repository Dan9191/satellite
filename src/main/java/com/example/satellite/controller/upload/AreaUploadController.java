package com.example.satellite.controller.upload;

import com.example.satellite.service.UploadAreaFileService;
import com.example.satellite.service.UploadFacilityFileService;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Controller
@RequestMapping("/v1/area")
@RequiredArgsConstructor
@Slf4j
public class AreaUploadController {

    /**
     * Сервис загрузки файлов.
     */
    private final UploadAreaFileService uploadFileService;

    @GetMapping
    public String blankConsole() {
        return "upload";
    }

    /**
     * Метод для загрузки
     */
    @PostMapping("/upload")
    public String fileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            uploadFileService.readFile(file);
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
