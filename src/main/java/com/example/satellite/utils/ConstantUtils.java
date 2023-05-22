package com.example.satellite.utils;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

public class ConstantUtils {

    public static String END_SATELLITE_PATTERN = ":  Access Summary Report";

    public static String FACILITY_NAME_PREFIX = "Facility-";

    public static String AREA_NAME_PREFIX = "AreaTarget-Russia-To-Satellite-KinoSat_";

    public static String SATELLITE_NAME_PREFIX = "Satellite-";

    public static String KINOSAT = "KinoSat";

    public static String ZORKIY = "Zorkiy";

    public static String TO = "-To-";

    /**
     * Маска для разложения дат.
     */
    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss.SSS", Locale.ENGLISH );;

    /**
     * Маска для поиска строк-расписаний.
     */
    public static Pattern SESSION_MATCHES_SIGN = Pattern.compile("^\\s+(\\d{1,3})(\\s+)(\\d{1,2})\\sJun\\s2027\\s(\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d)\\s+(\\d{1,2})\\sJun\\s2027\\s(\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d)\\s+(\\d{1,3}.\\d\\d\\d)$");
}
