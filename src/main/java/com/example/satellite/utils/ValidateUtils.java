package com.example.satellite.utils;

import static com.example.satellite.utils.ConstantUtils.KINOSAT;
import static com.example.satellite.utils.ConstantUtils.ZORKIY;

public class ValidateUtils {

    public static boolean kinosatValidName(String name) {
        return name.contains(KINOSAT);
    }

    public static boolean zorkiyValidName(String name) {
        return name.contains(ZORKIY);
    }
}
