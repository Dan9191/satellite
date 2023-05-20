package com.example.satellite.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunicationSession {

    private Integer number;

    private LocalDateTime startSessionTime;

    private LocalDateTime endSessionTime;

    private float duration;

}
