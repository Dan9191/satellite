package com.example.satellite.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunicationSession {

    private Integer number;

    private LocalDateTime startSessionTime;

    private LocalDateTime endSessionTime;

    private float duration;

}
