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

    private static final String FIND_NEXT_BY_TIME_SESSION = "select * from %s.satellite_area_session " +
            "where satellite_id = ? and start_session_time > ? limit 1";

    private static final String FIND_NEXT_BY_TIME_AND_FACILITY_SESSION =
            "select * from %s.satellite_area_session where satellite_id = ? " +
                    "and satellite_facility_session != null and start_session_time > " +
                    "? limit 1";


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

        Integer areaId = null;

        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format(FIND_BY_TIME_OVERLAP_SESSION, properties.getSchemaName()));

            ps.setInt(1, satellite.getId());
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(stop));
            ps.setTimestamp(4, Timestamp.valueOf(start));
            ps.setTimestamp(5, Timestamp.valueOf(stop));
            ps.setTimestamp(6, Timestamp.valueOf(stop));
            ps.setTimestamp(7, Timestamp.valueOf(start));
            ps.setTimestamp(8, Timestamp.valueOf(start));
            ps.setTimestamp(9, Timestamp.valueOf(stop));

            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                areaId = resultSet.getInt("id");
            }

        } catch (SQLException ex) {
            log.error("Find by time overlap query session failed.", ex);
            throw new RuntimeException("Find by time overlap query session failed.", ex);
        }

        return areaId;
    }

    @Override
    public Integer findNextByTime(Satellite satellite, LocalDateTime endSessionTime) {

        Integer nextAreaSessionId = 0;
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format(FIND_NEXT_BY_TIME_SESSION, properties.getSchemaName()));

            ps.setInt(1, satellite.getId());
            ps.setTimestamp(2, Timestamp.valueOf(endSessionTime));

            ResultSet resultSet = ps.executeQuery();
            while(resultSet.next()){
                nextAreaSessionId = resultSet.getInt("id");
            }
        } catch (SQLException ex){
            log.error("Find next by date query session failed.", ex);
            throw new RuntimeException("Find next by date query session failed.", ex);
        }
        return nextAreaSessionId;
    }

    @Override
    public Integer findNextByTimeAndFacilitySession(SatelliteAreaSession areaSession) {
        Satellite satellite = areaSession.getSatellite();
        int nextAreaSessionId = 0;
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            PreparedStatement ps = connection.prepareStatement(String.format(FIND_NEXT_BY_TIME_AND_FACILITY_SESSION, properties.getSchemaName()));

            ps.setInt(1, satellite.getId());
            ps.setTimestamp(2, Timestamp.valueOf(areaSession.getEndSessionTime()));

            ResultSet resultSet = ps.executeQuery();
            while(resultSet.next()){
                nextAreaSessionId = resultSet.getInt("id");
            }
        } catch (SQLException ex){
            log.error("Find next by date query session failed.", ex);
            throw new RuntimeException("Find next by date query session failed.", ex);
        }
        return nextAreaSessionId;
    }
}
