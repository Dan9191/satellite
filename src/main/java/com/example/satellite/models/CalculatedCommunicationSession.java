package com.example.satellite.models;

import com.example.satellite.entity.Satellite;
import com.example.satellite.entity.SatelliteAreaSession;
import com.example.satellite.entity.SatelliteFacilitySession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Модель для формирования сеанса спутника с приемником или съемкой.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalculatedCommunicationSession {

    /**
     * Порядковый номер сеанса.
     */
    private Integer number;

    /**
     * Спутник.
     */
    private Satellite owner;

    /**
     * Второй объект связи.
     */
    private String secondObject;

    /**
     * Начало сеанса.
     */
    private LocalDateTime startSessionTime;

    /**
     * Конец сеанса.
     */
    private LocalDateTime endSessionTime;

    /**
     * Продолжительность сеанса.
     */
    private float duration;

    /**
     * Изменение в памяти за сеанс.
     */
    private Long durationMemory;

    /**
     * Оставшаяся память на момент сеанса.
     */
    private Long currentMemory;

    /**
     * Объем переданной информации на момент сеанса.
     */
    private String memorySendingSum;

    /**
     * Конструктор сеанса, который пойдет в конечное расписание. Вычисляется из сеанса спутник-съемка.
     *
     * @param session        Данные сеанса
     * @param number         Порядковый номер сеанса.
     * @param durationMemory На сколько изменилась память.
     * @param currentMemory  Количество оставшейся памяти.
     */
    public CalculatedCommunicationSession(SatelliteAreaSession session,
                                          Integer number,
                                          Long durationMemory,
                                          Long currentMemory,
                                          String memorySendingSum) {
        this.number = number;
        this.owner = session.getSatellite();
        this.secondObject = session.getArea().getName();
        this.startSessionTime = session.getStartSessionTime();
        this.endSessionTime = session.getEndSessionTime();
        this.duration = session.getDuration();
        this.durationMemory = durationMemory;
        this.currentMemory = currentMemory;
        this.memorySendingSum = memorySendingSum;
    }

    /**
     * Конструктор сеанса, который пойдет в конечное расписание. Вычисляется из сеанса спутник-приемник.
     *
     * @param session        Данные сеанса
     * @param number         Порядковый номер сеанса.
     * @param durationMemory На сколько изменилась память.
     * @param currentMemory  Количество оставшейся памяти.
     */
    public CalculatedCommunicationSession(SatelliteFacilitySession session,
                                          Integer number,
                                          Long durationMemory,
                                          Long currentMemory,
                                          String memorySendingSum) {
        this.number = number;
        this.owner = session.getSatellite();
        this.secondObject = session.getFacility().getName();
        this.startSessionTime = session.getStartSessionTime();
        this.endSessionTime = session.getEndSessionTime();
        this.duration = session.getDuration();
        this.durationMemory = durationMemory;
        this.currentMemory = currentMemory;
        this.memorySendingSum = memorySendingSum;
    }

    @Override
    public String toString() {
        return String.format("%20s %30s %30s %20s %30s %30s %30s", secondObject,
                startSessionTime, endSessionTime, duration, durationMemory, currentMemory, memorySendingSum);
    }
}
