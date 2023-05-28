package com.example.satellite.entity;

import com.example.satellite.models.CommunicationSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "satellite_area_session")
public class SatelliteAreaSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "satellite_id", referencedColumnName = "id")
    private Satellite satellite;

    @ManyToOne
    @JoinColumn(name = "area_id", referencedColumnName = "id")
    private Area area;

    @Column(name = "order_number")
    private Integer orderNumber;

    @Column(name = "start_session_time")
    private LocalDateTime startSessionTime;

    @Column(name = "end_session_time")
    private LocalDateTime endSessionTime;


    @Column(name = "duration")
    private float duration;

    public SatelliteAreaSession(Satellite satellite, Area area, CommunicationSession sessionData) {
        this.satellite = satellite;
        this.area = area;
        this.orderNumber = sessionData.getNumber();
        this.startSessionTime = sessionData.getStartSessionTime();
        this.endSessionTime = sessionData.getEndSessionTime();
        this.duration = sessionData.getDuration();
    }

    @Override
    public String toString() {
        return String.format("%20s %30s %30s %20s", orderNumber, startSessionTime, endSessionTime, duration);
    }
}
