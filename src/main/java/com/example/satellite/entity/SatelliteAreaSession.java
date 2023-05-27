package com.example.satellite.entity;

import com.example.satellite.models.CommunicationSession;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
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

    @Column(name = "shot")
    private boolean shot;
    public SatelliteAreaSession(Satellite satellite, Area area, CommunicationSession sessionData) {
        this.satellite = satellite;
        this.area = area;
        this.orderNumber = sessionData.getNumber();
        this.startSessionTime = sessionData.getStartSessionTime();
        this.endSessionTime = sessionData.getEndSessionTime();
        this.duration = sessionData.getDuration();
    }

    @Column(name = "init_mem_status")
    private Long initialMemoryState;

    @Column(name = "fin_mem_status")
    private Long finalMemoryState;

    @OneToOne
    @PrimaryKeyJoinColumn (name = "satellite_facility_session")
    private SatelliteFacilitySession facilitySession;

    public SatelliteAreaSession() {

    }
}
