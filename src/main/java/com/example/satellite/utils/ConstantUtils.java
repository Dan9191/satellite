package com.example.satellite.utils;

import com.example.satellite.entity.SatelliteAreaSession;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Predicate;
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
     * Название файла с мини-отчетом.
     */
    public static String REPORT_DIR = "report";

    public static LocalTime START_SHOOTING_SESSION = LocalTime.parse("09:00:00");

    public static LocalTime END_SHOOTING_SESSION = LocalTime.parse("18:00:00");

    /**
     * Маска для разложения дат.
     */
    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss.SSS", Locale.ENGLISH );

    /**
     * Маска для поиска строк-расписаний.
     */
    public static Pattern SESSION_MATCHES_SIGN = Pattern.compile("^\\s+(\\d{1,3})(\\s+)(\\d{1,2})\\sJun\\s2027\\s(\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d)\\s+(\\d{1,2})\\sJun\\s2027\\s(\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d)\\s+(\\d{1,3}.\\d\\d\\d)$");

    /**
     * Предикат вычисляет, относится ли время съемки к дневному.
     */
    public static Predicate<SatelliteAreaSession> IS_SHOOTING_TIME = session ->
            session.getStartSessionTime().toLocalTime().isAfter(START_SHOOTING_SESSION)
            && session.getEndSessionTime().toLocalTime().isBefore(END_SHOOTING_SESSION);
}
