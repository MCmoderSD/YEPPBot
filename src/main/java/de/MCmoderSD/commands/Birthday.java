package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.utilities.database.MySQL;

import javax.management.InvalidAttributeValueException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Calendar;
import java.util.Comparator;

import static de.MCmoderSD.objects.Birthdate.TIME_ZONE;
import static de.MCmoderSD.utilities.other.Calculate.*;

public class Birthday {

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
    private HashMap<Integer, Birthdate> birthdays;

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

                // Get Birthday List
                birthdays = mySQL.getBirthdays();

                // Check Verb
                String verb = args.getFirst().toLowerCase();

                if (verb.startsWith("@")) {

                    // Check if tagged user
                    String response;
                    TwitchUser taggedUser = helixHandler.getUser(args.getFirst().substring(1).toLowerCase());
                    if (!birthdays.containsKey(taggedUser.getId())) response = "User hat noch keinen Geburtstag gesetzt.";
                    else response = String.format("@%s hat am %s Geburtstag! YEPP", taggedUser.getName(), birthdays.get(taggedUser.getId()).getDate());

                    // Response
                    botClient.respond(event, getCommand(), response);
                    return;
                }

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
            return "Invalid Date: " + date[0] + "." + date[1] + "." + date[2];
        }

        // Check Age (13+)
        if (!checkAge(13, birthdate)) return "Du musst mindestens 13 Jahre alt sein.";

        // Set Birthday
        mySQL.setBirthday(event, birthdate);
        botClient.getMessageHandler().updateBirthdateList(mySQL.getBirthdays());

        // Response
        return "Dein Geburtstag wurde auf " + date[0] + "." + date[1] + "." + date[2] + " gesetzt.";
    }

    private String nextBirthday(ArrayList<String> args) {

        // Variablen
        ArrayList<Birthdate> sortedBirthdays = new ArrayList<>(birthdays.values());
        int next = 0;

        // Check Argument
        if (args.size() > 1) {
            try {
                next = Integer.parseInt(args.get(1));
            } catch (NumberFormatException e) {
                return "Invalid Argument: " + syntax;
            }
        }

        // Sort
        sortedBirthdays.sort(Comparator.comparingInt(Birthdate::getDaysUntilBirthday));

        // Check if empty
        if (sortedBirthdays.isEmpty()) return "Es gibt keine gespeicherten Geburtstage.";

        // Get Next Birthday
        Birthdate nextBirthday = sortedBirthdays.get(next);
        String nextBirthdayUser = helixHandler.getUser(birthdays.entrySet().stream()
                .filter(entry -> entry.getValue().equals(nextBirthday))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new RuntimeException("User not found"))).getName();

        // Response
        return String.format("Der nächste Geburtstag ist von @%s am %s. YEPP", nextBirthdayUser, nextBirthday.getDayAndMonth());
    }

    private String listBirthday(ArrayList<String> args) {

        // Variablen
        List<Birthdate> sortedBirthdays = new ArrayList<>(birthdays.values());
        int next = 10;

        // Check Argument
        if (args.size() > 1) {
            try {
                next = Integer.parseInt(args.get(1));
            } catch (NumberFormatException e) {
                return "Invalid Argument: " + syntax;
            }
        }

        // Sort
        sortedBirthdays.sort(Comparator.comparingInt(Birthdate::getDaysUntilBirthday));

        // Check if empty
        if (sortedBirthdays.isEmpty()) return "Es gibt keine gespeicherten Geburtstage.";

        // Get Next Birthdays
        StringBuilder response = new StringBuilder("Die nächsten " + next + " Geburtstage sind: ");
        for (int i = 0; i < Math.min(next, sortedBirthdays.size()); i++) {
            Birthdate birthdate = sortedBirthdays.get(i);
            String username = helixHandler.getUser(birthdays.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(birthdate))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElseThrow(() -> new RuntimeException("User not found"))).getName();
            response.append(String.format("@%s am %s", username, birthdate.getDayAndMonth()));
            if (i < Math.min(next, sortedBirthdays.size()) - 1) response.append(", ");
        }
        response.append(". YEPP");

        // Response
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
        if (!birthdays.containsKey(id)) return "Dieser User hat noch keinen Geburtstag gesetzt.";

        // Get Birthday
        Birthdate birthdate = birthdays.get(id);
        return switch (args.get(tagged ? 2 : 1).toLowerCase()) {
            case "years", "jahre" -> String.format("Es sind %f Jahr%s bis zu %s Geburtstag.", birthdate.getYearsUntilBirthday(), birthdate.getYearsUntilBirthday() > 1 ? "e" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "months", "monate" -> String.format("Es sind %f Monat%s bis zu %s Geburtstag.", birthdate.getMonthsUntilBirthday(), birthdate.getMonthsUntilBirthday() > 1 ? "e" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "weeks", "wochen" -> String.format("Es sind %f Woche%s bis zu %s Geburtstag.", birthdate.getWeeksUntilBirthday(), birthdate.getWeeksUntilBirthday() > 1 ? "n" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "hours", "stunden", "h" -> String.format("Es sind %d Stunde%s bis zu %s Geburtstag.", birthdate.getHoursUntilBirthday(), birthdate.getHoursUntilBirthday() > 1 ? "n" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "days", "tage", "d" -> String.format("Es sind %s Tag%s bis zu %s Geburtstag.", birthdate.getDaysUntilBirthday(), birthdate.getDaysUntilBirthday() > 1 ? "e" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "seconds", "sekunden", "s" -> String.format("Es sind %d Sekunde%s bis zu %s Geburtstag.", birthdate.getSecondsUntilBirthday(), birthdate.getSecondsUntilBirthday() > 1 ? "n" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            case "milliseconds", "millisekunden", "ms" -> String.format("Es sind %d Millisekunde%s bis zu %s Geburtstag.", birthdate.getMillisecondsUntilBirthday(), birthdate.getMillisecondsUntilBirthday() > 1 ? "n" : "", tagged ? "@" + taggedUser + "'s" : "deinem");
            default -> "Invalid Argument: " + inSyntax;
        };
    }

    private String getBirthday(TwitchMessageEvent event, ArrayList<String> args) {

        // Check for Self
        if (args.size() == 1) {
            if (!birthdays.containsKey(event.getUserId())) return "Du hast noch keinen Geburtstag gesetzt.";
            else return "Dein Geburtstag ist am " + birthdays.get(event.getUserId()).getDate() + ". YEPP";
        }

        // Check for Date
        if (args.size() > 1 && args.get(1).split("\\.").length > 1) {
            try {
                // Search for Birthdate
                String[] searchDateParts = args.get(1).split("\\.");
                Birthdate searchBirthdate = new Birthdate(String.format("%s.%s.%s", searchDateParts[0], searchDateParts[1], searchDateParts.length == 3 ? searchDateParts[2] : "1990"));
                HashSet<Integer> searchBirthdays = new HashSet<>();
                for (Integer user: birthdays.keySet()) if (birthdays.get(user).getDayAndMonth().equals(searchBirthdate.getDayAndMonth())) searchBirthdays.add(user);

                // Check if found
                HashSet<String> usernames = new HashSet<>();
                for (int searchBirthday : searchBirthdays) usernames.add(helixHandler.getUser(searchBirthday).getName());

                // Response
                if (usernames.isEmpty()) return "Niemand hat am " + args.get(1) + " Geburtstag.";
                else return "Folgende User haben am " + args.get(1) + " Geburtstag: " + String.join(", ", usernames) + ". YEPP";
            } catch (InvalidAttributeValueException e) {
                    throw new RuntimeException(e);
            }
        }

        // Check for Tagged User
        if (args.size() > 1 && args.get(1).startsWith("@")) {
            TwitchUser taggedUser = helixHandler.getUser(args.get(1).substring(1).toLowerCase());
            if (!birthdays.containsKey(taggedUser.getId())) return "User hat noch keinen Geburtstag gesetzt.";
            else return String.format("@%s hat am %s Geburtstag! YEPP", taggedUser.getName(), birthdays.get(taggedUser.getId()).getDate());
        }

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
            default -> "Invalid Argument: " + getSyntax;
        };
    }

    // Get all birthdays in this month
    private String getAllBirthdaysInMonth(int month) {

        // Variables
        ArrayList<Map.Entry<Integer, Birthdate>> sortedBirthdays = new ArrayList<>(birthdays.entrySet());

        // Get Current Date
        Calendar today = Calendar.getInstance(TIME_ZONE);
        int currentYear = today.get(Calendar.YEAR);

        // Filter and sort birthdays in the specified month
        List<Map.Entry<Integer, Birthdate>> monthBirthdays = new ArrayList<>();
        for (Map.Entry<Integer, Birthdate> entry : sortedBirthdays) {
            Birthdate birthdate = entry.getValue();
            Calendar birthdateCalendar = Calendar.getInstance(TIME_ZONE);
            birthdateCalendar.set(Calendar.DAY_OF_MONTH, birthdate.getDay());
            birthdateCalendar.set(Calendar.MONTH, birthdate.getMonth() - 1);
            birthdateCalendar.set(Calendar.YEAR, currentYear);

            if (birthdateCalendar.get(Calendar.MONTH) == month) monthBirthdays.add(entry);
        }

        monthBirthdays.sort(Comparator.comparingInt(entry -> entry.getValue().getDay()));

        // Check if empty
        if (monthBirthdays.isEmpty()) return "Niemand hat in diesem Monat Geburtstag.";

        // Build response
        StringBuilder response = new StringBuilder("Folgende User haben in diesem Monat Geburtstag: ");
        for (Map.Entry<Integer, Birthdate> entry : monthBirthdays) {
            String username = helixHandler.getUser(entry.getKey()).getName();
            response.append(String.format("@%s am %s", username, entry.getValue().getDayAndMonth()));
            if (monthBirthdays.indexOf(entry) < monthBirthdays.size() - 1) response.append(", ");
        }
        response.append(". YEPP");

        return response.toString();
    }

    private String getThis(String verb) {

        // Variables
        ArrayList<Map.Entry<Integer, Birthdate>> sortedBirthdays = new ArrayList<>(birthdays.entrySet());
        HashSet<String> usernames = new HashSet<>();

        // Get Current Date
        Calendar today = Calendar.getInstance(TIME_ZONE);
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH);
        int currentWeek = today.get(Calendar.WEEK_OF_YEAR);

        try {
            switch (verb) {
                case "today", "heute" -> {
                    Birthdate todayBirthdate = new Birthdate(String.format("%d.%d.%d", today.get(Calendar.DAY_OF_MONTH), (today.get(Calendar.MONTH) + 1), today.get(Calendar.YEAR)));
                    for (int user : birthdays.keySet()) if (birthdays.get(user).getDayAndMonth().equals(todayBirthdate.getDayAndMonth())) usernames.add(helixHandler.getUser(user).getName());
                    if (usernames.isEmpty()) return "Niemand hat heute Geburtstag.";
                    else return "Folgende User haben heute Geburtstag: " + String.join(", ", usernames) + ". YEPP";
                }
                case "week", "woche" -> {
                    List<Map.Entry<Integer, Birthdate>> weekBirthdays = new ArrayList<>();
                    int currentDayOfYear = today.get(Calendar.DAY_OF_YEAR);

                    for (Map.Entry<Integer, Birthdate> entry : sortedBirthdays) {
                        Birthdate birthdate = entry.getValue();
                        Calendar birthdateCalendar = Calendar.getInstance(TIME_ZONE);
                        birthdateCalendar.set(Calendar.DAY_OF_MONTH, birthdate.getDay());
                        birthdateCalendar.set(Calendar.MONTH, birthdate.getMonth() - 1);
                        birthdateCalendar.set(Calendar.YEAR, currentYear);

                        if (birthdateCalendar.get(Calendar.WEEK_OF_YEAR) == currentWeek && birthdateCalendar.get(Calendar.DAY_OF_YEAR) >= currentDayOfYear) {
                            weekBirthdays.add(entry);
                        }
                    }

                    weekBirthdays.sort(Comparator.comparingInt(entry -> entry.getValue().getDay()));
                    if (weekBirthdays.isEmpty()) return "Niemand hat diese Woche Geburtstag.";
                    else {
                        StringBuilder response = new StringBuilder("Folgende User haben diese Woche Geburtstag: ");
                        for (Map.Entry<Integer, Birthdate> entry : weekBirthdays) {
                            String username = helixHandler.getUser(entry.getKey()).getName();
                            response.append(String.format("@%s am %s", username, entry.getValue().getDayAndMonth()));
                            if (weekBirthdays.indexOf(entry) < weekBirthdays.size() - 1) response.append(", ");
                        }
                        response.append(". YEPP");
                        return response.toString();
                    }
                }
                case "month", "monat" -> {
                    List<Map.Entry<Integer, Birthdate>> monthBirthdays = new ArrayList<>();
                    int currentDayOfMonth = today.get(Calendar.DAY_OF_MONTH);

                    for (Map.Entry<Integer, Birthdate> entry : sortedBirthdays) {
                        Birthdate birthdate = entry.getValue();
                        Calendar birthdateCalendar = Calendar.getInstance(TIME_ZONE);
                        birthdateCalendar.set(Calendar.DAY_OF_MONTH, birthdate.getDay());
                        birthdateCalendar.set(Calendar.MONTH, birthdate.getMonth() - 1);
                        birthdateCalendar.set(Calendar.YEAR, currentYear);

                        if (birthdateCalendar.get(Calendar.MONTH) == currentMonth && birthdateCalendar.get(Calendar.DAY_OF_MONTH) >= currentDayOfMonth) monthBirthdays.add(entry);
                    }

                    monthBirthdays.sort(Comparator.comparingInt(entry -> entry.getValue().getDay()));
                    if (monthBirthdays.isEmpty()) return "Niemand hat diesen Monat Geburtstag.";
                    else {
                        StringBuilder response = new StringBuilder("Folgende User haben diesen Monat Geburtstag: ");
                        for (Map.Entry<Integer, Birthdate> entry : monthBirthdays) {
                            String username = helixHandler.getUser(entry.getKey()).getName();
                            response.append(String.format("@%s am %s", username, entry.getValue().getDayAndMonth()));
                            if (monthBirthdays.indexOf(entry) < monthBirthdays.size() - 1) response.append(", ");
                        }
                        response.append(". YEPP");
                        return response.toString();
                    }
                }
                case "year", "jahr" -> {
                    List<Map.Entry<Integer, Birthdate>> yearBirthdays = new ArrayList<>();
                    int currentDayOfYear = today.get(Calendar.DAY_OF_YEAR);

                    for (Map.Entry<Integer, Birthdate> entry : sortedBirthdays) {
                        Birthdate birthdate = entry.getValue();
                        Calendar birthdateCalendar = Calendar.getInstance(TIME_ZONE);
                        birthdateCalendar.set(Calendar.DAY_OF_MONTH, birthdate.getDay());
                        birthdateCalendar.set(Calendar.MONTH, birthdate.getMonth() - 1);
                        birthdateCalendar.set(Calendar.YEAR, currentYear);
                        if (birthdateCalendar.get(Calendar.DAY_OF_YEAR) >= currentDayOfYear) yearBirthdays.add(entry);
                    }

                    yearBirthdays.sort(Comparator.comparingInt(entry -> entry.getValue().getDaysUntilBirthday()));
                    if (yearBirthdays.isEmpty()) return "Niemand hat dieses Jahr Geburtstag.";
                    else {
                        StringBuilder response = new StringBuilder("Folgende User haben dieses Jahr Geburtstag: ");
                        for (Map.Entry<Integer, Birthdate> entry : yearBirthdays) {
                            String username = helixHandler.getUser(entry.getKey()).getName();
                            response.append(String.format("@%s am %s", username, entry.getValue().getDayAndMonth()));
                            if (yearBirthdays.indexOf(entry) < yearBirthdays.size() - 1) response.append(", ");
                        }
                        response.append(". YEPP");
                        return response.toString();
                    }
                }
                default -> {
                    return ("Invalid Argument: " + syntax);
                }
            }
        } catch (InvalidAttributeValueException e) {
            throw new RuntimeException(e);
        }
    }
}