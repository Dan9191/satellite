package com.example.satellite.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportsRow {

    /**
     * Спутник.
     */
    private String owner;

    /**
     * Дата перегрузки памяти.
     */
    private Optional<LocalDateTime> overflowTime;

    /**
     * Общий объем переданных данных.
     */
    private String allMemorySendingSum;

    @Override
    public String toString() {
        String localDate = overflowTime.map(LocalDateTime::toString).orElse("Спутник не был перегружен");
        return String.format("%30s %30s %30s", owner, localDate, allMemorySendingSum);
    }
}
