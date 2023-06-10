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
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
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

    @Transient
    private String dataMb;

    public SatelliteFacilitySession(Satellite satellite, Facility facility, CommunicationSession session) {
        this.satellite = satellite;
        this.facility = facility;
        this.orderNumber = session.getNumber();
        this.startSessionTime = session.getStartSessionTime();
        this.endSessionTime = session.getEndSessionTime();
        this.duration = session.getDuration();
    }

    @Override
    public String toString() {
        return String.format("%20s %30s %20s %20s %20s", startSessionTime, endSessionTime, duration,
                satellite.getName(), dataMb);
    }
}
