package de.MCmoderSD.objects;

import javax.management.InvalidAttributeValueException;
import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings("unused")
public class Birthdate {

    // Set Timezone
    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Berlin");

    // Constants
    private final byte day;
    private final byte month;
    private final short year;

    // Constructor
    public Birthdate(String date) throws InvalidAttributeValueException, NumberFormatException {

        // Split Date
        String[] split = date.split("\\.");
        if (split.length != 3) throw new InvalidAttributeValueException("Invalid date: " + date);

        // Parse Date
        byte day = Byte.parseByte(split[0]);
        byte month = Byte.parseByte(split[1]);
        short year = Short.parseShort(split[2]);

        // Check Month
        if (month < 1 || month > 12) throw new InvalidAttributeValueException("Invalid month: " + month);

        // Check Day
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                if (day < 1 || day > 31) throw new InvalidAttributeValueException("Invalid day: " + day);
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                if (day < 1 || day > 30) throw new InvalidAttributeValueException("Invalid day: " + day);
                break;
            case 2:
                if (year % 4 == 0) {
                    if (day < 1 || day > 29) throw new InvalidAttributeValueException("Invalid day: " + day);
                } else if (day < 1 || day > 28) throw new InvalidAttributeValueException("Invalid day: " + day);
                break;
        }

        // Check Year
        if (year < 0 || year > (short) Calendar.getInstance(TIME_ZONE).get(Calendar.YEAR))
            throw new InvalidAttributeValueException("Invalid year: " + year);

        // Set Values
        this.day = day;
        this.month = (byte) (month - 1);
        this.year = year;
    }

    // Get Date
    public String getDate() {
        return day + "." + (month + 1) + "." + year;
    }

    // Get Day and Month
    public String getDayAndMonth() {
        return day + "." + (month + 1);
    }

    // Get Day
    public byte getDay() {
        return day;
    }

    // Get Month
    public byte getMonth() {
        return (byte) (month + 1);
    }

    // Get Year
    public short getYear() {
        return year;
    }

    // Get Age
    public byte getAge() {
        byte age = (byte) (Calendar.getInstance(TIME_ZONE).get(Calendar.YEAR) - year);
        if (Calendar.getInstance(TIME_ZONE).get(Calendar.MONTH) < month) age--;
        else if (Calendar.getInstance(TIME_ZONE).get(Calendar.MONTH) == month && Calendar.getInstance(TIME_ZONE).get(Calendar.DAY_OF_MONTH) < day) age--;
        return age;
    }

    public float getYearsUntilBirthday() {
        return getDaysUntilBirthday() / 365f;
    }

    public float getMonthsUntilBirthday() {
        return getDaysUntilBirthday() / 365f;
    }

    public float getWeeksUntilBirthday() {
        return getDaysUntilBirthday() / 7f;
    }

    public int getDaysUntilBirthday() {
        Calendar today = Calendar.getInstance(TIME_ZONE);
        Calendar nextBirthday = Calendar.getInstance(TIME_ZONE);
        nextBirthday.set(Calendar.DAY_OF_MONTH, day);
        nextBirthday.set(Calendar.MONTH, month);
        if (today.after(nextBirthday)) nextBirthday.add(Calendar.YEAR, 1);
        long diff = nextBirthday.getTimeInMillis() - today.getTimeInMillis();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    public int getHoursUntilBirthday() {
        return getDaysUntilBirthday() * 24;
    }

    public int getMinutesUntilBirthday() {
        return getHoursUntilBirthday() * 60;
    }

    public int getSecondsUntilBirthday() {
        return getMinutesUntilBirthday() * 60;
    }

    public int getMillisecondsUntilBirthday() {
        return getSecondsUntilBirthday() * 1000;
    }

    // Is Birthday
    public boolean isBirthday() {
        return Calendar.getInstance(TIME_ZONE).get(Calendar.DAY_OF_MONTH) == day && Calendar.getInstance(TIME_ZONE).get(Calendar.MONTH) == month;
    }

    // Get Zodiac Sign
    public String getZodiacSign() {

        // Zodiac Signs
        if ((month == 0 && day >= 20) || (month == 1 && day <= 18)) return "aquarius";
        if (month == 1 || month == 2 && day <= 20) return "pisces";
        if (month == 2 || month == 3 && day <= 19) return "aries";
        if (month == 3 || month == 4 && day <= 20) return "taurus";
        if (month == 4 || month == 5 && day <= 20) return "gemini";
        if (month == 5 || month == 6 && day <= 22) return "cancer";
        if (month == 6 || month == 7 && day <= 22) return "leo";
        if (month == 7 || month == 8 && day <= 22) return "virgo";
        if (month == 8 || month == 9 && day <= 22) return "libra";
        if (month == 9 || month == 10 && day <= 21) return "scorpio";
        if (month == 10 || month == 11 && day <= 21) return "sagittarius";
        if (month == 11 || month == 0) return "capricorn";

        // Default
        return "Unknown";
    }

    public TimeZone getTimeZone() {
        return TIME_ZONE;
    }
}
