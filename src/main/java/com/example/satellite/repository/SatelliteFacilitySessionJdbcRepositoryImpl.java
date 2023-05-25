package com.example.satellite.repository;

import com.example.satellite.config.SatelliteProperties;
import com.example.satellite.entity.SatelliteFacilitySession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * Настройки приложения.
     */
    private final SatelliteProperties properties;


    private static final String INSERT_SESSION = "insert into %s.satellite_facility_session("
            + "satellite_id, facility_id, order_number, start_session_time, end_session_time, duration"
            + ") values (?, ?, ?, ?, ?, ?) returning id";

    private static final String FIND_PREVIOUS_BY_DATE_SESSION = "select * from %s.satellite_facility_session " +
            "where end_session_time < ? limit 1";


    /**
     * Пакетное сохранение сеансов связи.
     *
     * @param sessionList сеансов связи
     */
    @Override
    public void saveBatch(List<SatelliteFacilitySession> sessionList) {
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format(INSERT_SESSION, properties.getSchemaName()),
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

    @Override
    public Integer findPreviousByDate(SatelliteFacilitySession facilitySession) {
        Integer nextAreaSessionId = 0;
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format(FIND_PREVIOUS_BY_DATE_SESSION, properties.getSchemaName()));

            ps.setObject(1, facilitySession.getStartSessionTime());

            ResultSet resultSet = ps.executeQuery();
            while(resultSet.next()){
                nextAreaSessionId = resultSet.getInt("Id");
            }
        } catch (SQLException ex){
            log.error("Find next by date query session failed.", ex);
            throw new RuntimeException("Find next by date query session failed.", ex);
        }
        return nextAreaSessionId;
    }

}
