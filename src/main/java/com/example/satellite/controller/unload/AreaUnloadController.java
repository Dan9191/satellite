package com.example.satellite.controller.unload;

import com.example.satellite.service.unload.UnloadAreaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/v1/unload")
@RequiredArgsConstructor
public class AreaUnloadController {

    /**
     * Сервис для формирования файла выйгрузки расписания по траекториям.
     */
    private final UnloadAreaFileService unloadAreaFileService;

    @GetMapping("/area")
    @ResponseBody
    public String getFoos(@RequestParam("name") String areaName) {
        unloadAreaFileService.unloadFile(areaName);
        return "area: " + areaName;
    }
}
