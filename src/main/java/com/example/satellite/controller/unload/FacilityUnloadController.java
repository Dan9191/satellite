package com.example.satellite.controller.unload;

import com.example.satellite.service.unload.UnloadFacilityFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/v1/unload")
@RequiredArgsConstructor
public class FacilityUnloadController {

    /**
     * Сервис для формирования файла выйгрузки расписания по траекториям.
     */
    private final UnloadFacilityFileService unloadFacilityFileService;

    @GetMapping("/facility")
    @ResponseBody
    public String getFoos(@RequestParam("name") String facilityName) {
        unloadFacilityFileService.unloadFile(facilityName);
        return "facilityName: " + facilityName;
    }
}
