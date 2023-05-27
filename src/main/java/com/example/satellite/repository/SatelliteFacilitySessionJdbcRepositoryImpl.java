package com.example.satellite.repository;

import com.example.satellite.config.SatelliteProperties;
import com.example.satellite.entity.SatelliteFacilitySession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SatelliteFacilitySessionJdbcRepositoryImpl implements SatelliteFacilitySessionJdbcRepository {

    /**
     * Jdbc-шаблон.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Название схемы.
     */
    private String schemaName;

    private static final String INSERT_SESSION = "insert into %s.satellite_facility_session("
            + "satellite_id, facility_id, order_number, start_session_time, end_session_time, duration"
            + ") values (?, ?, ?, ?, ?, ?) returning id";

    @Autowired
    public SatelliteFacilitySessionJdbcRepositoryImpl(JdbcTemplate jdbcTemplate,
                                                  @Value("${spring.flyway.schemas}") String schemaName) {
        this.jdbcTemplate = jdbcTemplate;
        this.schemaName = schemaName;
    }

    /**
     * Пакетное сохранение сеансов связи.
     *
     * @param sessionList сеансов связи
     */
    @Override
    public void saveBatch(List<SatelliteFacilitySession> sessionList) {
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format(INSERT_SESSION, schemaName),
                    Statement.RETURN_GENERATED_KEYS);

            for (SatelliteFacilitySession session : sessionList) {
                ps.setObject(1, session.getSatellite().getId(), Types.INTEGER);
                ps.setObject(2, session.getFacility().getId(), Types.INTEGER);
                ps.setObject(3, session.getOrderNumber().floatValue(), Types.INTEGER);
                ps.setObject(4, session.getStartSessionTime(), Types.TIMESTAMP);
                ps.setObject(5, session.getEndSessionTime(), Types.TIMESTAMP);
                ps.setObject(6, session.getDuration(), Types.FLOAT);
                ps.addBatch();
            }
            ps.executeBatch();

            Iterator<SatelliteFacilitySession> iterator = sessionList.iterator();
            ResultSet keys = ps.getGeneratedKeys();

            // updating ids
            while (keys.next()) {
                int id = keys.getInt(1);
                SatelliteFacilitySession next = iterator.next();
                next.setId(id);
            }
        } catch (SQLException e) {
            log.error("CommunicationSession saving error", e);
            throw new RuntimeException("CommunicationSession saving error", e);
        }
    }

}
