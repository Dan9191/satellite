package com.example.satellite.models;

import com.example.satellite.entity.SatelliteAreaSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Условное обозначение суток.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Day {

    /**
     * День, в который происходят сеансы съемок.
     */
    private String name;

    /**
     * Список сеансов съемки, которые возможны в этот день.
     */
    private List<SatelliteAreaSession> areaSessionList;
}
