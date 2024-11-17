package de.MCmoderSD.objects;

import de.MCmoderSD.enums.ZodiacSign;

import javax.management.InvalidAttributeValueException;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings({"unused", "SameReturnValue"})
public class Birthdate {

    // Set Timezone
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Berlin");

    // Constants
    private final int year;
    private final MonthDay date;
    private final ZodiacSign zodiacSign;

    // Constructor
    public Birthdate(String date) throws InvalidAttributeValueException, NumberFormatException {

        // Split Date
        String[] split = date.split("\\.");
        if (split.length != 3) throw new InvalidAttributeValueException("Invalid date: " + date);

        // Parse Date
        byte day = Byte.parseByte(split[0]);
        byte month = Byte.parseByte(split[1]);
        short year = Short.parseShort(split[2]);

        // Check and set Date
        this.date = MonthDay.of(month, day);

        // Check Year
        if (year < 1 || year > Calendar.getInstance(TIME_ZONE).get(Calendar.YEAR)) throw new InvalidAttributeValueException("Invalid year: " + year);
        else this.year = year;

        // Set Zodiac Sign
        zodiacSign = ZodiacSign.getZodiacSign(this.date);
    }

    // Getter
    public boolean isBirthday() {
        LocalDate today = LocalDate.now(TIME_ZONE.toZoneId());
        return today.getMonthValue() == date.getMonthValue() && today.getDayOfMonth() == date.getDayOfMonth();
    }

    // Get Day
    public int getDay() {
        return date.getDayOfMonth();
    }

    // Get Month
    public int getMonth() {
        return date.getMonthValue();
    }

    // Get Year
    public int getYear() {
        return year;
    }

    // Get MM/DD/YYYY
    public String getDate() {
        return getDay() + "." + getMonth() + "." + getYear();
    }

    // Get YYYY.MM.DD
    public String getMySQLDate() {
        return getYear() + "." + getMonth() + "." + getDay();
    }

    // Get DD/MM
    public String getDayMonth() {
        return getDay() + "." + getMonth();
    }

    // Get MonthDay
    public MonthDay getMonthDay() {
        return date;
    }

    public TimeZone getTimeZone() {
        return TIME_ZONE;
    }

    public ZodiacSign getZodiacSign() {
        return zodiacSign;
    }

    public int getAge() {
        LocalDate today = LocalDate.now(TIME_ZONE.toZoneId());
        LocalDate birthDate = LocalDate.of(year, date.getMonthValue(), date.getDayOfMonth());
        return Period.between(birthDate, today).getYears();
    }

    public long getNanosecondsUntilBirthday() {
        return getMicrosecondsUntilBirthday() * 1000L;
    }

    public long getMicrosecondsUntilBirthday() {
        return getMillisecondsUntilBirthday() * 1000L;
    }

    public long getMillisecondsUntilBirthday() {
        return getSecondsUntilBirthday() * 1000L;
    }

    public int getSecondsUntilBirthday() {
        return getMinutesUntilBirthday() * 60;
    }

    public int getMinutesUntilBirthday() {
        return getHoursUntilBirthday() * 60;
    }

    public int getHoursUntilBirthday() {
        return getDaysUntilBirthday() * 24;
    }

    public int getDaysUntilBirthday() {

        // Get Birthday
        LocalDate today = LocalDate.now(TIME_ZONE.toZoneId());
        LocalDate birthday = LocalDate.of(today.getYear(), date.getMonthValue(), date.getDayOfMonth());

        // If birthday has already occurred this year, set it to next year
        if (today.isAfter(birthday) || today.isEqual(birthday)) birthday = birthday.plusYears(1);

        return (int) ChronoUnit.DAYS.between(today, birthday);
    }

    public float getWeeksUntilBirthday() {
        return getDaysUntilBirthday() / 7f;
    }

    public float getMonthsUntilBirthday() {
        return getYearsUntilBirthday() * 12f;
    }

    public float getYearsUntilBirthday() {
        return getDaysUntilBirthday() / 365f;
    }
}