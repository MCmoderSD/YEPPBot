package de.MCmoderSD.data;

import java.time.MonthDay;
import java.util.Calendar;

public record Birthdate(int day, int month, int year) {

    // Constants
    private static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);

    // Constructor
    public Birthdate {
        if (day < 1 || day > 31) throw new IllegalArgumentException("Day must be between 1 and 31.");
        if (month < 1 || month > 12) throw new IllegalArgumentException("Month must be between 1 and 12.");
        if (year < 1900 || year > CURRENT_YEAR) throw new IllegalArgumentException("Year must be between 1900 and " + CURRENT_YEAR + ".");
        if (month == 2 && day == 29 && !isLeapYear(year)) throw new IllegalArgumentException("February 29 is only valid in a leap year.");
        if (month == 2 && day > 29) throw new IllegalArgumentException("February has only 28 days, or 29 in a leap year.");
        if (month == 4 && day > 30) throw new IllegalArgumentException("April has only 30 days.");
        if (month == 6 && day > 30) throw new IllegalArgumentException("June has only 30 days.");
        if (month == 9 && day > 30) throw new IllegalArgumentException("September has only 30 days.");
        if (month == 11 && day > 30) throw new IllegalArgumentException("November has only 30 days.");
    }

    // Helper Methods
    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    // Methods
    public MonthDay getMonthDay() {
        return MonthDay.of(month, day);
    }

    public String getFormattedDate() {
        return String.format("%02d.%02d.%04d", day, month, year);
    }

    public boolean isToday() {
        return getMonthDay().equals(MonthDay.now());
    }

    public int getAge() {
        var today = MonthDay.now();
        var birthMonthDay = getMonthDay();
        var age = CURRENT_YEAR - year;
        if (today.isBefore(birthMonthDay)) age--;
        return age;
    }
}