package com.example.satellite.utils;

/**
 * Утилита для работы с памятью
 */
public class MemoryUtils {

    /**
     * Безопасное сложение памяти, для избегания переполнения.
     *
     * @param currentMemory       Текущая память спутника.
     * @param sessionMemoryIncome На сколько должна измениться ().
     * @param TOTAL_MEMORY        Общий объем памяти у спутника.
     * @return Результат суммирования.
     */
    public static long memorySum(long currentMemory, long sessionMemoryIncome, long TOTAL_MEMORY) {
        long result = currentMemory + sessionMemoryIncome;
        return Math.min(result, TOTAL_MEMORY);

    }

    /**
     * Безопасное вычитание памяти спутника для избегания отрицательных чисел при сеансе съемки.
     *
     * @param currentMemory         Текущая память.
     * @param sessionMemorySpending На сколько должна измениться.
     * @return Результат вычитания.
     */
    public static long memorySubtraction(long currentMemory, long sessionMemorySpending) {
        long result = currentMemory - sessionMemorySpending;
        return Math.max(result, 0L);
    }

    /**
     * Безопасное вычисление реально переданной информации.
     *
     * @param fullSessionDeltaMemory Расчетный объем данных за сессию.
     * @param currentMemory          Состояние памяти спутника до передачи данных.
     * @param memoryAfterSending     Состояние спутника после передачи данных.
     * @return Количество реально переданных данных.
     */
    public static long actuallySendingMemory(long fullSessionDeltaMemory, long currentMemory, long memoryAfterSending) {
        long realDelta = memoryAfterSending - currentMemory;
        return Math.min(fullSessionDeltaMemory, realDelta);
    }


    public static String readableSize(long fullSessionDeltaMemory) {
        String cnt_size;
        long size_kb = fullSessionDeltaMemory /1024;
        long size_mb = size_kb / 1024;
        long size_gb = size_mb / 1024 ;
        if (size_gb > 0){
            cnt_size = size_gb + " GB";
        }else if(size_mb > 0){
            cnt_size = size_mb + " MB";
        }else{
            cnt_size = size_kb + " KB";
        }
        return cnt_size;
    }

}
