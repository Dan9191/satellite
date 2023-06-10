package com.example.satellite.utils;

import static com.example.satellite.utils.ConstantUtils.*;

/**
 * Утилита для классификации спутника.
 */
public class ValidateUtils {

    public static boolean kinosatValidName(String satelliteNumber) {
        return SATELLITE_TYPE.indexOf(satelliteNumber) < 5;
    }

    public static boolean zorkiyValidName(String satelliteNumber) {
        return SATELLITE_TYPE.indexOf(satelliteNumber) > 4;
    }
}
