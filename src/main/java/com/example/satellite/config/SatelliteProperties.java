package com.example.satellite.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

/**
 * Настройки приложения.
 */
@Data
@ConfigurationProperties("satellite")
public class SatelliteProperties {

    /**
     * Название схемы.
     */
    private String schemaName = "sat";

    /**
     * Папка для сохранения выгрузки в альтернативном формате.
     */
    private File alternativeFormatDirectory;

    /**
     * Папка для сохранения расписаний спутников по траектории.
     */
    private File facilityDirectory;


    /**
     * Папка для сохранения расписаний работы приемников.
     */
    private File areaDirectory;

    /**
     * Папка для отчета.
     */
    private File reportDirectory;
}
