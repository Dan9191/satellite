package com.example.satellite.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки приложения.
 */
@Data
@ConfigurationProperties("satellite")
public class SatelliteProperties {

    private String schemaName;
}
