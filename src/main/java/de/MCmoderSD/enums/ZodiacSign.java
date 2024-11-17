package de.MCmoderSD.enums;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.json.JsonUtility;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.MonthDay;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

// Zodiac Sign
@SuppressWarnings("unused")
public enum ZodiacSign {

    // Constants
    ARIES(MonthDay.of(3, 21), MonthDay.of(4, 20)),
    TAURUS(MonthDay.of(4, 21), MonthDay.of(5, 20)),
    GEMINI(MonthDay.of(5, 21), MonthDay.of(6, 21)),
    CANCER(MonthDay.of(6, 22), MonthDay.of(7, 22)),
    LEO(MonthDay.of(7, 23), MonthDay.of(8, 23)),
    VIRGO(MonthDay.of(8, 24), MonthDay.of(9, 23)),
    LIBRA(MonthDay.of(9, 24), MonthDay.of(10, 23)),
    SCORPIO(MonthDay.of(10, 24), MonthDay.of(11, 22)),
    SAGITTARIUS(MonthDay.of(11, 23), MonthDay.of(12, 21)),
    CAPRICORN(MonthDay.of(12, 22), MonthDay.of(1, 20)),
    AQUARIUS(MonthDay.of(1, 21), MonthDay.of(2, 19)),
    PISCES(MonthDay.of(2, 20), MonthDay.of(3, 20));

    // Attributes
    private final MonthDay startDate;
    private final MonthDay endDate;
    private final Iterator<Map.Entry<String, JsonNode>> matches;

    // Static Attributes
    private ZodiacSign[] compatibleSigns;

    // Constructor
    ZodiacSign(MonthDay startDate, MonthDay endDate) {

        // Set Attributes
        this.startDate = startDate;
        this.endDate = endDate;

        // Load Matches
        try {
            matches = JsonUtility.loadJson("/assets/matchList.json", false).get(getName()).fields();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to load match list for " + getName() + "!", e);
        }
    }

    // Static Initializer
    static {
        ARIES.compatibleSigns = new ZodiacSign[]{GEMINI, LEO, SAGITTARIUS};
        TAURUS.compatibleSigns = new ZodiacSign[]{CANCER, LIBRA, PISCES};
        GEMINI.compatibleSigns = new ZodiacSign[]{GEMINI, LIBRA, SAGITTARIUS};
        CANCER.compatibleSigns = new ZodiacSign[]{VIRGO, SCORPIO, PISCES};
        LEO.compatibleSigns = new ZodiacSign[]{LEO, VIRGO, LIBRA};
        VIRGO.compatibleSigns = new ZodiacSign[]{TAURUS, SCORPIO, CAPRICORN};
        LIBRA.compatibleSigns = new ZodiacSign[]{GEMINI, LEO, AQUARIUS};
        SCORPIO.compatibleSigns = new ZodiacSign[]{CANCER, VIRGO, CAPRICORN};
        SAGITTARIUS.compatibleSigns = new ZodiacSign[]{SAGITTARIUS, AQUARIUS, PISCES};
        CAPRICORN.compatibleSigns = new ZodiacSign[]{CAPRICORN, TAURUS, PISCES};
        AQUARIUS.compatibleSigns = new ZodiacSign[]{ARIES, GEMINI, AQUARIUS};
        PISCES.compatibleSigns = new ZodiacSign[]{TAURUS, CANCER, CAPRICORN};
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

    public ZodiacSign[] getCompatibleSigns() {
        return compatibleSigns;
    }

    public ZodiacSign getCompatibleSign(int index) {
        return compatibleSigns[index];
    }

    public String getTranslatedName() {
        return switch (name().toLowerCase()) {
            case "aquarius" -> "Wassermann";
            case "pisces" -> "Fische";
            case "aries" -> "Widder";
            case "taurus" -> "Stier";
            case "gemini" -> "Zwillinge";
            case "cancer" -> "Krebs";
            case "leo" -> "Löwe";
            case "virgo" -> "Jungfrau";
            case "libra" -> "Waage";
            case "scorpio" -> "Skorpion";
            case "sagittarius" -> "Schütze";
            case "capricorn" -> "Steinbock";
            default -> "Unbekannt";
        };
    }

    public LinkedHashMap<ZodiacSign, String> getMatches() {
        LinkedHashMap<ZodiacSign, String> matches = new LinkedHashMap<>();
        this.matches.forEachRemaining(entry -> matches.put(getZodiacSign(entry.getKey()), entry.getValue().asText()));
        return matches;
    }

    public String getMatchDescription(ZodiacSign sign) {
        return getMatches().get(sign);
    }

    // Static Methods
    public static ZodiacSign getZodiacSign(String name) {
        for (ZodiacSign zodiacSign : values()) if (zodiacSign.getName().equalsIgnoreCase(name)) return zodiacSign;
        return null;
    }

    public static ZodiacSign getZodiacSign(int month, int day) {
        return getZodiacSign(MonthDay.of(month, day));
    }

    public static ZodiacSign getZodiacSign(MonthDay monthDay) {
        for (ZodiacSign sign : ZodiacSign.values()) {
            if (sign.getStartDate().isBefore(sign.getEndDate())) {

                // Norm al case: start date is before end date within the same year
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
}