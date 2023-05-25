package com.example.satellite.repository;

import com.example.satellite.config.SatelliteProperties;
import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


@Slf4j
@Component
@RequiredArgsConstructor
public class SatelliteAreaSessionJdbcRepositoryImpl implements SatelliteAreaSessionJdbcRepository {

    /**
     * Jdbc-шаблон.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Настройки приложения.
     */
    private final SatelliteProperties properties;

    private static final String INSERT_SESSION = "insert into %s.satellite_area_session("
            + "satellite_id, area_id, order_number, start_session_time, end_session_time, duration"
            + ") values (?, ?, ?, ?, ?, ?) returning id";

    private static final String FIND_BY_TIME_OVERLAP_SESSION = "select * from %s.satellite_area_session " +
            "where satellite_id = ? " +
            "and (start_session_time <= ? and end_session_time >= ?) " +
            "or (start_session_time >= ? and start_session_time <= ? and end_session_time >= ?) " +
            "or (start_session_time <= ? and end_session_time >= ? and end_session_time <= ?)";


    /**
     * Пакетное сохранение сеансов связи.
     *
     * @param sessionList сеансов связи
     */
    @Override
    public void saveBatch(List<SatelliteAreaSession> sessionList) {
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format(INSERT_SESSION, properties.getSchemaName()),
                    Statement.RETURN_GENERATED_KEYS);

            for (SatelliteAreaSession session : sessionList) {
                ps.setObject(1, session.getSatellite().getId(), Types.INTEGER);
                ps.setObject(2, session.getArea().getId(), Types.INTEGER);
                ps.setObject(3, session.getOrderNumber().floatValue(), Types.INTEGER);
                ps.setObject(4, session.getStartSessionTime(), Types.TIMESTAMP);
                ps.setObject(5, session.getEndSessionTime(), Types.TIMESTAMP);
                ps.setObject(6, session.getDuration(), Types.FLOAT);
                ps.addBatch();
            }
            ps.executeBatch();

            Iterator<SatelliteAreaSession> iterator = sessionList.iterator();
            ResultSet keys = ps.getGeneratedKeys();

            // updating ids
            while (keys.next()) {
                int id = keys.getInt(1);
                SatelliteAreaSession next = iterator.next();
                next.setId(id);
            }
        } catch (SQLException e) {
            log.error("CommunicationSession saving error", e);
            throw new RuntimeException("CommunicationSession saving error", e);
        }
    }

    @Override
    public Integer findByTimeOverlap(Satellite satellite, LocalDateTime start, LocalDateTime stop) {
        Integer areaSessionId = 0;
        if (satellite == null)
            return 0;
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format(FIND_BY_TIME_OVERLAP_SESSION, properties.getSchemaName()));

            ps.setObject(1, satellite.getId());
            ps.setObject(2, start);
            ps.setObject(3, stop);
            ps.setObject(4, start);
            ps.setObject(5, stop);
            ps.setObject(6, stop);
            ps.setObject(7, start);
            ps.setObject(8, start);
            ps.setObject(9, stop);

            ResultSet resultSet = ps.executeQuery();
            while(resultSet.next()){
                areaSessionId = resultSet.getInt("Id");
            }
        } catch (SQLException ex){
            log.error("Find by time overlap query session failed.", ex);
            throw new RuntimeException("Find by time overlap query session failed.", ex);
        }

        return areaSessionId;
    }
}
