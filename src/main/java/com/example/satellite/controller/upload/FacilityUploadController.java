package com.example.satellite.controller.upload;

import com.example.satellite.service.UploadFacilityFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/v1/facility")
@RequiredArgsConstructor
@Slf4j
public class FacilityUploadController {

    /**
     * Сервис загрузки файлов.
     */
    private final UploadFacilityFileService uploadFileService;

    /**
     * Метод для загрузки
     */
    @PostMapping("/upload")
    public ResponseEntity fileUpload(@RequestParam("file") MultipartFile file) {
        String tableName = file.getOriginalFilename();

        try {
            uploadFileService.readFile(file);
        } catch (IOException e) {
            log.error("Can't read file", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body(tableName);
    }
}
