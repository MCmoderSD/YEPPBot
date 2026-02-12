package de.MCmoderSD.enums;

import de.MCmoderSD.json.JsonUtility;
import tools.jackson.databind.JsonNode;

import java.io.Serializable;
import java.time.MonthDay;
import java.util.*;

@SuppressWarnings("unused")
public enum ZodiacSign implements Serializable {

    // Enum Constants
    ARIES(MonthDay.of(3, 21), MonthDay.of(4, 20)),          // 21. March - 20. April
    TAURUS(MonthDay.of(4, 21), MonthDay.of(5, 20)),         // 21. April - 20. May
    GEMINI(MonthDay.of(5, 21), MonthDay.of(6, 21)),         // 21. May - 21. June
    CANCER(MonthDay.of(6, 22), MonthDay.of(7, 22)),         // 22. June - 22. July
    LEO(MonthDay.of(7, 23), MonthDay.of(8, 23)),            // 23. July - 23. August
    VIRGO(MonthDay.of(8, 24), MonthDay.of(9, 23)),          // 24. August - 23. September
    LIBRA(MonthDay.of(9, 24), MonthDay.of(10, 23)),         // 24. September - 23. October
    SCORPIO(MonthDay.of(10, 24), MonthDay.of(11, 22)),      // 24. October - 22. November
    SAGITTARIUS(MonthDay.of(11, 23), MonthDay.of(12, 21)),  // 23. November - 21. December
    CAPRICORN(MonthDay.of(12, 22), MonthDay.of(1, 20)),     // 22. December - 20. January
    AQUARIUS(MonthDay.of(1, 21), MonthDay.of(2, 19)),       // 21. January - 19. February
    PISCES(MonthDay.of(2, 20), MonthDay.of(3, 20));         // 20. February - 20. March

    // Attributes
    private final MonthDay startDate;
    private final MonthDay endDate;
    private final LinkedHashMap<ZodiacSign, String> matches;

    // Constructor
    ZodiacSign(MonthDay startDate, MonthDay endDate) {

        // Set Attributes
        this.startDate = startDate;
        this.endDate = endDate;

        // Initialize matches
        matches = new LinkedHashMap<>(3);
    }

    static {

        // Load match list from JSON file
        JsonNode matchList = JsonUtility.getInstance().loadResource("/assets/matchList.json");

        // Populate matches for each zodiac sign
        for (var sign : ZodiacSign.values()) {
            for (var match : getMatchingSigns(sign)) {
                sign.addMatch(match, matchList.get(sign.getName()).get(match.getName()).asString());
            }
        }
    }

    private void addMatch(ZodiacSign zodiacSign, String description) {
        matches.put(zodiacSign, description);
    }

    // Methods
    public String getName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public MonthDay getStartDate() {
        return startDate;
    }

    public MonthDay getEndDate() {
        return endDate;
    }

    public LinkedHashMap<ZodiacSign, String> getMatches() {
        return matches;
    }

    public boolean isMatch(ZodiacSign zodiacSign) {
        return matches.containsKey(zodiacSign);
    }

    public String getMatchDescription(ZodiacSign zodiacSign) {
        return matches.get(zodiacSign);
    }

    // Static Methods
    public static ZodiacSign getZodiacSign(int month, int day) {
        return getZodiacSign(MonthDay.of(month, day));
    }

    public static ZodiacSign getZodiacSign(MonthDay monthDay) {
        for (var sign : ZodiacSign.values()) {
            if (sign.getStartDate().isBefore(sign.getEndDate())) {

                // Normal case: start date is before end date within the same year
                if ((monthDay.isAfter(sign.getStartDate()) || monthDay.equals(sign.getStartDate())) && (monthDay.isBefore(sign.getEndDate()) || monthDay.equals(sign.getEndDate()))) {
                    return sign;
                }

            } else {

                // Special case: zodiac sign spans the end and start of the year
                if ((monthDay.isAfter(sign.getStartDate()) || monthDay.equals(sign.getStartDate())) || (monthDay.isBefore(sign.getEndDate()) || monthDay.equals(sign.getEndDate()))) {
                    return sign;
                }
            }
        }

        // Invalid date
        throw new IllegalArgumentException("Invalid date: " + monthDay);
    }

    public static ZodiacSign[] getMatchingSigns(ZodiacSign zodiacSign) {
        return switch (zodiacSign) {
            case ARIES -> new ZodiacSign[]{GEMINI, LEO, SAGITTARIUS};
            case TAURUS -> new ZodiacSign[]{CANCER, LIBRA, PISCES};
            case GEMINI -> new ZodiacSign[]{GEMINI, LIBRA, SAGITTARIUS};
            case CANCER -> new ZodiacSign[]{VIRGO, SCORPIO, PISCES};
            case LEO -> new ZodiacSign[]{LEO, VIRGO, LIBRA};
            case VIRGO -> new ZodiacSign[]{TAURUS, SCORPIO, CAPRICORN};
            case LIBRA -> new ZodiacSign[]{GEMINI, LEO, AQUARIUS};
            case SCORPIO -> new ZodiacSign[]{CANCER, VIRGO, CAPRICORN};
            case SAGITTARIUS -> new ZodiacSign[]{SAGITTARIUS, AQUARIUS, PISCES};
            case CAPRICORN -> new ZodiacSign[]{CAPRICORN, TAURUS, PISCES};
            case AQUARIUS -> new ZodiacSign[]{ARIES, GEMINI, AQUARIUS};
            case PISCES -> new ZodiacSign[]{TAURUS, CANCER, CAPRICORN};
        };
    }
}