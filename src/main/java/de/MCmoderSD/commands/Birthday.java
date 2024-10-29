package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.other.Calculate;

import javax.management.InvalidAttributeValueException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Birthday {

    // Messages for set
    private final String invalidDate;
    private final String ageRestriction;
    private final String birthWasSetOn;

    // Messages for get
    private final String yourBirthdayIsOn;
    private final String youHaveNoBirthdaySet;
    private final String userHasBirthdayOn;
    private final String userHasNoBirthdaySet;
    private final String nobodyHasBirthdayOn;
    private final String followingUsersHaveBirthdayOn;
    private final String userOn;

    // Messages for next
    private final String noSavedBirthdays;
    private final String theNextBirthday;
    private final String nextBirthdaysAre;

    // Messages for getAll
    private final String followingUsersHaveBirthdayInThisYear;
    private final String followingUsersHaveBirthdayInThisMonth;
    private final String nobodyHasBirthdayInThisYear;
    private final String nobodyHasBirthdayInThisMonth;

    // Messages for getThis
    private final String followingUsersHaveBirthdayToday;
    private final String followingUsersHaveBirthdayThisWeek;
    private final String followingUsersHaveBirthDayThisMonth;
    private final String followingUsersHaveBirthdayThisYear;
    private final String nobodyHasBirthdayToday;
    private final String nobodyHasBirthdayThisWeek;
    private final String nobodyHasBirthdayThisMonth;
    private final String nobodyHasBirthdayThisYear;

    // Messages for inBirthday
    private final String msgYears;
    private final String msgMonths;
    private final String msgWeeks;
    private final String msgDays;
    private final String msgHours;
    private final String msgSeconds;
    private final String msgMilliseconds;
    private final String msgMicroseconds;
    private final String msgNanoseconds;

    // Association
    private final BotClient botClient;
    private final MySQL mySQL;
    private final HelixHandler helixHandler;

    // Attributes
    private final String syntax;
    private final String setSyntax;
    private final String getSyntax;
    private final String inSyntax;

    // Variables
    private LinkedHashMap<Integer, Birthdate> birthdays;

    // Constructor
    public Birthday(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, HelixHandler helixHandler) {

        // Syntax
        syntax = "Syntax: " + botClient.getPrefix() + "birthday set/get/in/list/next";
        setSyntax = "Syntax: " + botClient.getPrefix() + "birthday set DD.MM.CCYY";
        getSyntax = "Syntax: " + botClient.getPrefix() + "birthday get [DD.MM | @user | Year | Month ]";
        inSyntax = "Syntax: " + botClient.getPrefix() + "birthday in <user> [ Months | Days | Hours ]";

        // About
        String[] name = {"birthday", "bday", "geburtstag", "bd", "geb", "gb"};
        String description = "Setzt deinen Geburtstag. " + syntax;


        // Messages for set
        invalidDate = "Invalid Date: %s.%s.%s";
        ageRestriction = "Du musst mindestens 13 Jahre alt sein.";
        birthWasSetOn = "Dein Geburtstag wurde auf den %s gesetzt.";

        // Messages for get
        userHasNoBirthdaySet = "User hat noch keinen Geburtstag gesetzt.";
        userHasBirthdayOn = "@%s hat am %s Geburtstag! YEPP";
        youHaveNoBirthdaySet = "Du hast noch keinen Geburtstag gesetzt.";
        yourBirthdayIsOn = "Dein Geburtstag ist am %s. YEPP";
        nobodyHasBirthdayOn = "Niemand hat am %s Geburtstag.";
        followingUsersHaveBirthdayOn = "Folgende User haben am %s Geburtstag: %s. YEPP";
        userOn = "@%s am %s";

        // Messages for next and list
        noSavedBirthdays = "Es gibt keine gespeicherten Geburtstage.";
        theNextBirthday = "Der nächste Geburtstag ist von @%s am %s. YEPP";
        nextBirthdaysAre = "Die nächsten %d Geburtstage sind: ";

        // Messages for getAll
        followingUsersHaveBirthdayInThisYear = "Folgende User haben in diesem Jahr Geburtstag: ";
        followingUsersHaveBirthdayInThisMonth = "Folgende User haben in diesem Monat noch Geburtstag: ";
        nobodyHasBirthdayInThisYear = "Niemand hat in diesem Jahr Geburtstag.";
        nobodyHasBirthdayInThisMonth = "Niemand hat in diesem Monat Geburtstag.";

        // Messages for getThis
        followingUsersHaveBirthdayToday = "Folgende User haben heute Geburtstag: ";
        followingUsersHaveBirthdayThisWeek = "Folgende User haben diese Woche noch Geburtstag: ";
        followingUsersHaveBirthDayThisMonth = "Folgende User haben diesen Monat noch Geburtstag: ";
        followingUsersHaveBirthdayThisYear = "Folgenden Usern haben diesen Jahr noch Geburtstag: ";
        nobodyHasBirthdayToday = "Niemand hat heute Geburtstag.";
        nobodyHasBirthdayThisWeek = "Niemand hat diese Woche noch Geburtstag.";
        nobodyHasBirthdayThisMonth = "Niemand hat diesen Monat noch Geburtstag.";
        nobodyHasBirthdayThisYear = "Niemand hat diesen Jahr noch Geburtstag.";

        // Messages for inBirthday
        msgYears = "Es sind %f Jahr%s bis zu %s Geburtstag.";
        msgMonths = "Es sind %f Monat%s bis zu %s Geburtstag.";
        msgWeeks = "Es sind %f Woche%s bis zu %s Geburtstag.";
        msgDays = "Es sind %s Tag%s bis zu %s Geburtstag.";
        msgHours = "Es sind %d Stunde%s bis zu %s Geburtstag.";
        msgSeconds = "Es sind %d Sekunde%s bis zu %s Geburtstag.";
        msgMilliseconds = "Es sind %d Millisekunde%s bis zu %s Geburtstag.";
        msgMicroseconds = "Es sind %d Mikrosekunde%s bis zu %s Geburtstag.";
        msgNanoseconds = "Es sind %d Nanosekunde%s bis zu %s Geburtstag.";


        // Association
        this.botClient = botClient;
        this.mySQL = mySQL;
        this.helixHandler = helixHandler;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Check Arguments
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }


                // Get Birthdays
                birthdays = Calculate.getBirthdayList(event, botClient).entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.comparing(Birthdate::getDaysUntilBirthday)))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


                // Check Verb
                String verb = args.getFirst().toLowerCase();

                // Check for Tagged User
                if (verb.startsWith("@")) {

                    // Check if tagged user
                    String response;
                    TwitchUser taggedUser = helixHandler.getUser(args.getFirst().substring(1).toLowerCase());
                    if (!birthdays.containsKey(taggedUser.getId())) response = userHasNoBirthdaySet;
                    else response = String.format(userHasBirthdayOn, taggedUser.getName(), birthdays.get(taggedUser.getId()).getDate());

                    // Response
                    botClient.respond(event, getCommand(), response);
                    return;
                }

                // Check Verb
                switch (verb) {
                    case "set":
                        botClient.respond(event, getCommand(), setBirthday(event, args));
                        break;
                    case "next":
                        botClient.respond(event, getCommand(), nextBirthday(args));
                        break;
                    case "list":
                        botClient.respond(event, getCommand(), listBirthday(args));
                        break;
                    case "in":
                        botClient.respond(event, getCommand(), inBirthday(event, args));
                        break;
                    case "get":
                        botClient.respond(event, getCommand(), getBirthday(event, args));
                        break;
                    default:
                        botClient.respond(event, getCommand(), syntax);
                        break;
                }
            }
        });
    }

    private String setBirthday(TwitchMessageEvent event, ArrayList<String> args) {

        // Check Syntax
        if (args.size() != 2) return setSyntax;

        // Check Birthday
        String[] date = args.get(1).split("\\.");
        if (date.length != 3) return setSyntax;

        // Set Birthday
        Birthdate birthdate;
        try {
            birthdate = new Birthdate(args.get(1));
        } catch (InvalidAttributeValueException | NumberFormatException e) {
            return String.format(invalidDate, date[0], date[1], date[2]);
        }

        // Check Age (13+)
        if (birthdate.getAge() < 13) return ageRestriction;

        // Set Birthday
        mySQL.setBirthday(event, birthdate);
        botClient.getMessageHandler().updateBirthdateList(mySQL.getBirthdays());

        // Response
        return String.format(birthWasSetOn, birthdate.getDate());
    }

    private String nextBirthday(ArrayList<String> args) {

        // Variablen
        var next = 0;

        // Check Argument
        if (args.size() > 1) {
            try {
                next = Integer.parseInt(args.get(1));
            } catch (NumberFormatException e) {
                return "Invalid Argument: " + syntax;
            }
        }

        // Check if empty
        if (birthdays.isEmpty()) return noSavedBirthdays;

        // Debug Print Full Birthday List
        birthdays.forEach((id, birthdate) -> System.out.println(id + ": " + birthdate.getDate()));

        // Get User-Id
        var nextBirthdayUserId = new ArrayList<>(birthdays.keySet()).get(next);

        // Get Username
        TwitchUser nextBirthdayUser = helixHandler.getUser(nextBirthdayUserId);

        // Response
        return String.format(theNextBirthday, nextBirthdayUser.getName(), birthdays.get(nextBirthdayUserId).getDayMonth());
    }

    private String listBirthday(ArrayList<String> args) {

        // Variables
        var next = 10;

        // Check Argument
        if (args.size() > 1) {
            try {
                next = Integer.parseInt(args.get(1));
            } catch (NumberFormatException e) {
                return "Invalid Argument: " + syntax;
            }
        }

        // Check if empty
        if (birthdays.isEmpty()) return noSavedBirthdays;


        // Get User IDs
        ArrayList<Integer> ids = new ArrayList<>(birthdays.keySet());
        while (ids.size() > next) ids.removeLast();

        // Check if empty
        if (ids.isEmpty()) return noSavedBirthdays;

        // Get Usernames
        HashSet<TwitchUser> users = helixHandler.getUsersByID(new HashSet<>(ids));
        LinkedHashMap<Integer, String> usernameMap = new LinkedHashMap<>();
        for (var id : ids) usernameMap.put(id, users.stream().filter(user -> user.getId() == id).findFirst().orElseThrow().getName());

        // Check if empty
        if (usernameMap.isEmpty()) return noSavedBirthdays;

        // Build response
        StringBuilder response = new StringBuilder(String.format(nextBirthdaysAre, next));
        for (var id : ids) response.append(String.format(userOn, usernameMap.get(id), birthdays.get(id).getDate())).append(", ");
        response.setLength(response.length() - 2); // Remove trailing comma and space
        response.append(". YEPP");

        // Check if response is too long
        while (response.length() > 500) {
            next--;
            ids.removeLast();
            response = new StringBuilder(String.format(nextBirthdaysAre, next));
            for (var id : ids) response.append(String.format(userOn, usernameMap.get(id), birthdays.get(id).getDate())).append(", ");
            response.setLength(response.length() - 2); // Remove trailing comma and space
            response.append(". YEPP");
        }

        // Return response
        return response.toString();
    }

    private String inBirthday(TwitchMessageEvent event, ArrayList<String> args) {

        // Variables
        boolean tagged = args.size() == 3;
        String taggedUser = null;

        // Check Args
        if (args.size() < 2) return inSyntax;
        if (tagged) taggedUser = args.get(1).startsWith("@") ? args.get(1).substring(1).toLowerCase() : args.get(1).toLowerCase();
        var id = tagged ? helixHandler.getUser(taggedUser).getId() : event.getUserId();

        // Check Birthday
        if (!birthdays.containsKey(id)) return tagged ? userHasNoBirthdaySet : youHaveNoBirthdaySet;

        // Get Birthday
        Birthdate birthdate = birthdays.get(id);
        return switch (args.get(tagged ? 2 : 1).toLowerCase()) {
            case "years", "jahre" -> String.format(msgYears, birthdate.getYearsUntilBirthday(), birthdate.getYearsUntilBirthday() > 1 ? "e" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "months", "monate" -> String.format(msgMonths, birthdate.getMonthsUntilBirthday(), birthdate.getMonthsUntilBirthday() > 1 ? "e" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "weeks", "wochen" -> String.format(msgWeeks, birthdate.getWeeksUntilBirthday(), birthdate.getWeeksUntilBirthday() > 1 ? "n" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "hours", "stunden", "h" -> String.format(msgDays, birthdate.getHoursUntilBirthday(), birthdate.getHoursUntilBirthday() > 1 ? "n" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "days", "tage", "d" -> String.format(msgHours, birthdate.getDaysUntilBirthday(), birthdate.getDaysUntilBirthday() > 1 ? "e" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "seconds", "sekunden", "s" -> String.format(msgSeconds, birthdate.getSecondsUntilBirthday(), birthdate.getSecondsUntilBirthday() > 1 ? "n" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "milliseconds", "millisekunden", "ms" -> String.format(msgMilliseconds, birthdate.getMillisecondsUntilBirthday(), birthdate.getMillisecondsUntilBirthday() > 1 ? "n" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "microseconds", "mikrosekunden", "µs" -> String.format(msgMicroseconds, birthdate.getMicrosecondsUntilBirthday(), birthdate.getMicrosecondsUntilBirthday() > 1 ? "n" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "nanoseconds", "nanosekunden", "ns" -> String.format(msgNanoseconds, birthdate.getNanosecondsUntilBirthday(), birthdate.getNanosecondsUntilBirthday() > 1 ? "n" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            default -> "Invalid Argument: " + inSyntax;
        };
    }

    private String getBirthday(TwitchMessageEvent event, ArrayList<String> args) {

        // Check for Self
        if (args.size() == 1) {
            if (!birthdays.containsKey(event.getUserId())) return youHaveNoBirthdaySet;
            else return String.format(yourBirthdayIsOn, birthdays.get(event.getUserId()).getDate());
        }

        // Check for Date
        if (args.size() > 1 && args.get(1).split("\\.").length > 1) {
            try {
                // Search for Birthdate
                String[] searchDateParts = args.get(1).split("\\.");
                for (String searchDatePart : searchDateParts) Integer.parseInt(searchDatePart);
                Birthdate searchBirthdate = new Birthdate(String.format("%s.%s.%s", searchDateParts[0], searchDateParts[1], searchDateParts.length == 3 ? searchDateParts[2] : "1990"));
                HashSet<Integer> searchBirthdays = new HashSet<>();
                for (Integer user: birthdays.keySet()) if (birthdays.get(user).getDayMonth().equals(searchBirthdate.getDayMonth())) searchBirthdays.add(user);

                // Check if found
                HashSet<String> usernames = new HashSet<>();
                helixHandler.getUsersByID(searchBirthdays).forEach(user -> usernames.add(user.getName()));

                // Response
                if (usernames.isEmpty()) return String.format(nobodyHasBirthdayOn, searchBirthdate.getDayMonth());
                else return String.format(followingUsersHaveBirthdayOn, searchBirthdate.getDayMonth(), String.join(", ", usernames));
            } catch (InvalidAttributeValueException | NumberFormatException e) {
                    return "Invalid Argument: " + getSyntax;
            }
        }

        // Check for Tagged User
        if (args.size() > 1 && args.get(1).startsWith("@")) {
            TwitchUser taggedUser = helixHandler.getUser(args.get(1).substring(1).toLowerCase());
            if (!birthdays.containsKey(taggedUser.getId())) return userHasNoBirthdaySet;
            else return String.format(userHasBirthdayOn, taggedUser.getName(), birthdays.get(taggedUser.getId()).getDate());
        }

        try {
            return switch (args.get(1).toLowerCase()) {
                case "today", "heute" -> getThis("today");
                case "week", "woche" -> getThis("week");
                case "month", "monat" -> getThis("month");
                case "year", "jahr" -> getThis("year");
                case "january", "januar", "jänner", "1" -> getAllBirthdaysInMonth(0);
                case "february", "februar", "2" -> getAllBirthdaysInMonth(1);
                case "march", "märz", "3" -> getAllBirthdaysInMonth(2);
                case "april", "4" -> getAllBirthdaysInMonth(3);
                case "may", "mai", "5" -> getAllBirthdaysInMonth(4);
                case "june", "juni", "juno", "6" -> getAllBirthdaysInMonth(5);
                case "july", "juli", "7" -> getAllBirthdaysInMonth(6);
                case "august", "8" -> getAllBirthdaysInMonth(7);
                case "september", "9" -> getAllBirthdaysInMonth(8);
                case "october", "oktober", "10" -> getAllBirthdaysInMonth(9);
                case "november", "11" -> getAllBirthdaysInMonth(10);
                case "december", "dezember", "12" -> getAllBirthdaysInMonth(11);
                default -> getAllBirthdaysInYear(Integer.parseInt(args.get(1)));
            };
        } catch (NumberFormatException e) {
            return "Invalid Argument: " + getSyntax;
        }
    }

    // Get all birthdays in this year
    private String getAllBirthdaysInYear(int year) {

        // Variables
        ArrayList<Integer> yearBirthdays = new ArrayList<>();
        for (var user : birthdays.keySet()) if (birthdays.get(user).getYear() == year) yearBirthdays.add(user);

        // Check if empty
        if (yearBirthdays.isEmpty()) return nobodyHasBirthdayInThisYear;

        // Get Usernames
        HashSet<TwitchUser> usernames = helixHandler.getUsersByID(new HashSet<>(yearBirthdays));
        LinkedHashMap<Integer, String> usernameMap = new LinkedHashMap<>();
        for (var user : usernames) usernameMap.put(user.getId(), user.getName());

        // Build response
        StringBuilder response = new StringBuilder(followingUsersHaveBirthdayInThisYear);
        for (var user : yearBirthdays) response.append(String.format(userOn, usernameMap.get(user), birthdays.get(user).getDate())).append(", ");
        response.setLength(response.length() - 2); // Remove trailing comma and space
        response.append(". YEPP");

        // Return response
        return response.toString();
    }

    // Get all birthdays in this month
    private String getAllBirthdaysInMonth(int month) {

        // Variables
        ArrayList<Integer> monthBirthdays = new ArrayList<>();
        for (var user : birthdays.keySet()) if (birthdays.get(user).getMonth() == month) monthBirthdays.add(user);

        // Check if empty
        if (monthBirthdays.isEmpty()) return nobodyHasBirthdayInThisMonth;

        // Get Usernames
        HashSet<TwitchUser> usernames = helixHandler.getUsersByID(new HashSet<>(monthBirthdays));
        LinkedHashMap<Integer, String> usernameMap = new LinkedHashMap<>();
        for (var user : usernames) usernameMap.put(user.getId(), user.getName());

        // Build response
        StringBuilder response = new StringBuilder(String.format(followingUsersHaveBirthdayInThisMonth));
        for (var user : monthBirthdays) response.append(String.format(userOn, usernameMap.get(user), birthdays.get(user).getDate())).append(", ");
        response.setLength(response.length() - 2); // Remove trailing comma and space
        response.append(". YEPP");

        // Return response
        return response.toString();
    }

    private String getThis(String verb) {
        switch (verb) {
            case "today", "heute" -> {

                // Variables
                MonthDay today = MonthDay.now();
                HashSet<Integer> ids = new HashSet<>();
                HashSet<String> usernames = new HashSet<>();

                // Add all users with birthday today to the ids set
                for (var user : birthdays.keySet()) if (birthdays.get(user).getMonthDay().equals(today)) ids.add(user);

                // Check if empty
                if (ids.isEmpty()) return nobodyHasBirthdayToday;

                // Get Usernames
                HashSet<TwitchUser> users = helixHandler.getUsersByID(ids);

                // Add all usernames to the usernames set
                users.forEach(user -> usernames.add(user.getName()));
                if (usernames.isEmpty()) return nobodyHasBirthdayToday;

                // Build response
                else return String.format(followingUsersHaveBirthdayToday, String.join(", ", usernames));
            }
            case "week", "woche" -> {

                // Variables
                LocalDate today = LocalDate.now();
                var currentWeek = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
                LinkedHashMap<Integer, Birthdate> weekBirthdays = new LinkedHashMap<>();

                // Add all users with birthday this week to the weekBirthdays map
                for (var user : birthdays.entrySet()) {
                    LocalDate birthdate = user.getValue().getMonthDay().atYear(today.getYear());
                    if (birthdate.get(WeekFields.ISO.weekOfWeekBasedYear()) == currentWeek && birthdate.isAfter(today)) weekBirthdays.put(user.getKey(), user.getValue());
                }

                // Check if empty
                if (weekBirthdays.isEmpty()) return nobodyHasBirthdayThisWeek;

                // Get Usernames
                HashSet<TwitchUser> usernames = helixHandler.getUsersByID(new HashSet<>(weekBirthdays.keySet()));
                LinkedHashMap<Integer, String> usernameMap = new LinkedHashMap<>();
                for (var user : usernames) usernameMap.put(user.getId(), user.getName());

                // Check if empty
                if (usernameMap.isEmpty()) return nobodyHasBirthdayThisWeek;

                // Build response
                StringBuilder response = new StringBuilder(followingUsersHaveBirthdayThisWeek);
                for (var id : weekBirthdays.keySet()) response.append(String.format(userOn, usernameMap.get(id), weekBirthdays.get(id).getDate())).append(", ");
                response.setLength(response.length() - 2); // Remove trailing comma and space
                response.append(". YEPP");

                // Check if response is too long
                while (response.length() > 498) {
                    weekBirthdays.remove(weekBirthdays.keySet().stream().findFirst().orElseThrow());
                    response = new StringBuilder(followingUsersHaveBirthdayThisWeek);
                    for (var id : weekBirthdays.keySet()) response.append(String.format(userOn, usernameMap.get(id), weekBirthdays.get(id).getDate())).append(", ");
                    response.setLength(response.length() - 2); // Remove trailing comma and space
                    response.append("... YEPP");
                }

                // Return response
                return response.toString();
            }
            case "month", "monat" -> {

                // Variables
                MonthDay today = MonthDay.now();
                MonthDay beginOfNextMonth = today.getMonth().getValue() == 12 ? MonthDay.of(1, 1) : MonthDay.of(today.getMonthValue() + 1, 1);

                // Create a LinkedHashMap with all users that have birthday this month
                LinkedHashMap <Integer, Birthdate> monthBirthdays = new LinkedHashMap<>();
                for (var user : birthdays.keySet())
                    if (today.isBefore(birthdays.get(user).getMonthDay()) && beginOfNextMonth.isAfter(birthdays.get(user).getMonthDay()))
                        monthBirthdays.put(user, birthdays.get(user));

                if (monthBirthdays.isEmpty()) return nobodyHasBirthdayThisMonth;

                // Get Usernames
                HashSet<TwitchUser> usernames = helixHandler.getUsersByID(new HashSet<>(monthBirthdays.keySet()));
                LinkedHashMap<Integer, String> usernameMap = new LinkedHashMap<>();
                for (var user : usernames) usernameMap.put(user.getId(), user.getName());

                // Check if empty
                if (usernameMap.isEmpty()) return nobodyHasBirthdayThisMonth;

                // Build response
                StringBuilder response = new StringBuilder(followingUsersHaveBirthDayThisMonth);
                for (var id : monthBirthdays.keySet()) response.append(String.format(userOn, usernameMap.get(id), monthBirthdays.get(id).getDate())).append(", ");
                response.setLength(response.length() - 2); // Remove trailing comma and space
                response.append(". YEPP");

                // Check if response is too long
                while (response.length() > 498) {
                    monthBirthdays.remove(monthBirthdays.keySet().stream().findFirst().orElseThrow());
                    response = new StringBuilder(followingUsersHaveBirthDayThisMonth);
                    for (var id : monthBirthdays.keySet()) response.append(String.format(userOn, usernameMap.get(id), monthBirthdays.get(id).getDate())).append(", ");
                    response.setLength(response.length() - 2); // Remove trailing comma and space
                    response.append("... YEPP");
                }

                // Return response
                return response.toString();
            }
            case "year", "jahr" -> {

                // Variables
                MonthDay today = MonthDay.now();
                MonthDay endOfYear = MonthDay.of(12, 31);
                LinkedHashMap<Integer, Birthdate> yearBirthdays = new LinkedHashMap<>();

                // Add all users with birthday this year to the yearBirthdays map
                for (var user : birthdays.keySet())
                    if (today.isBefore(birthdays.get(user).getMonthDay()) && endOfYear.isAfter(birthdays.get(user).getMonthDay()) || endOfYear.equals(birthdays.get(user).getMonthDay()))
                        yearBirthdays.put(user, birthdays.get(user));

                // Check if empty
                if (yearBirthdays.isEmpty()) return nobodyHasBirthdayThisYear;

                // Get Usernames
                HashSet<TwitchUser> usernames = helixHandler.getUsersByID(new HashSet<>(yearBirthdays.keySet()));
                LinkedHashMap<Integer, String> usernameMap = new LinkedHashMap<>();
                for (var user : usernames) usernameMap.put(user.getId(), user.getName());

                // Check if empty
                if (usernameMap.isEmpty()) return nobodyHasBirthdayThisYear;

                // Build response
                StringBuilder response = new StringBuilder(followingUsersHaveBirthdayThisYear);
                for (var id : yearBirthdays.keySet()) response.append(String.format(userOn, usernameMap.get(id), yearBirthdays.get(id).getDate())).append(", ");
                response.setLength(response.length() - 2); // Remove trailing comma and space
                response.append(". YEPP");

                // Check if response is too long
                while (response.length() > 498) {
                    yearBirthdays.remove(yearBirthdays.keySet().stream().findFirst().orElseThrow());
                    response = new StringBuilder(followingUsersHaveBirthdayThisYear);
                    for (var id : yearBirthdays.keySet()) response.append(String.format(userOn, usernameMap.get(id), yearBirthdays.get(id).getDate())).append(", ");
                    response.setLength(response.length() - 2); // Remove trailing comma and space
                    response.append("... YEPP");
                }

                // Return response
                return response.toString();
            }
            default -> {
                return ("Invalid Argument: " + syntax);
            }
        }
    }
}