package com.example.satellite.entity;

import com.example.satellite.models.CommunicationSession;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "satellite_facility_session")
public class SatelliteFacilitySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "satellite_id", referencedColumnName = "id")
    private Satellite satellite;

    @ManyToOne
    @JoinColumn(name = "facility_id", referencedColumnName = "id")
    private Facility facility;

    @Column(name = "order_number")
    private Integer orderNumber;

    @Column(name = "start_session_time")
    private LocalDateTime startSessionTime;

    @Column(name = "end_session_time")
    private LocalDateTime endSessionTime;


    @Column(name = "duration")
    private float duration;

    @OneToOne
    @PrimaryKeyJoinColumn
    private SatelliteAreaSession areaSession;

    public SatelliteFacilitySession(Satellite satellite, Facility facility, CommunicationSession session) {
        this.satellite = satellite;
        this.facility = facility;
        this.orderNumber = session.getNumber();
        this.startSessionTime = session.getStartSessionTime();
        this.endSessionTime = session.getEndSessionTime();
        this.duration = session.getDuration();
    }

    public SatelliteFacilitySession() {

    }
}
