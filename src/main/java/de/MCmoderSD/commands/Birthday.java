package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.utilities.database.MySQL;

import javax.management.InvalidAttributeValueException;
import java.util.*;

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

    // Constructor
    public Birthday(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, HelixHandler helixHandler) {

        // Syntax
        syntax = "Syntax: " + botClient.getPrefix() + "birthday set/list/get/next/<user> <DD.MM.CCYY>/<days|hours|seconds>";
        setSyntax = "Syntax: " + botClient.getPrefix() + "birthday set <DD.MM.CCYY>";

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

                // Format Arguments
                args.removeIf(String::isBlank);

                // Check Arguments
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Check Verb
                String verb = args.getFirst().toLowerCase();

                if (verb.startsWith("@")) {

                    // Check if tagged user
                    HashMap<Integer, Birthdate> birthdays = mySQL.getBirthdays();
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
                    case "list":
                        botClient.respond(event, getCommand(), listBirthday(args));
                        break;
                    case "get":
                        botClient.respond(event, getCommand(), getBirthday(event, args));
                        break;
                    case "next":
                        botClient.respond(event, getCommand(), nextBirthday(args));
                        break;
                    case "today":
                    case "heute":
                    case "month":
                    case "monat":
                    case "year":
                    case "jahr":
                    case "week":
                    case "woche":
                        botClient.respond(event, getCommand(), getThis(verb));
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
        if (!checkAge((byte) 13, birthdate)) return "Du musst mindestens 13 Jahre alt sein.";

        // Set Birthday
        mySQL.setBirthday(event, birthdate);
        botClient.getMessageHandler().updateBirthdateList(mySQL.getBirthdays());

        // Response
        return "Dein Geburtstag wurde auf " + date[0] + "." + date[1] + "." + date[2] + " gesetzt.";
    }

    private String listBirthday(ArrayList<String> args) {

        // Variables
        HashMap<Integer, Birthdate> birthdays = mySQL.getBirthdays();
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

    private String getBirthday(TwitchMessageEvent event, ArrayList<String> args) {

        // Variables
        HashMap<Integer, Birthdate> birthdays = mySQL.getBirthdays();
        var id = event.getUserId();

        // Check Birthday
        if (!birthdays.containsKey(id)) return "Du hast noch keinen Geburtstag gesetzt.";

        // Get Birthday
        Birthdate birthdate = birthdays.get(id);

        // Check if tagged user
        if (args.size() > 1 && args.get(1).split("\\.").length == 2) {

            try {

                // Search for Birthdate
                Birthdate searchBirthdate = new Birthdate(args.get(1) + ".1990");
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

        } else if (args.size() > 1 && Arrays.asList("years", "jahre", "months", "monate", "weeks", "wochen", "hours", "stunden", "h", "days", "tage", "d", "seconds", "sekunden", "s", "milliseconds", "millisekunden", "ms").contains(args.get(1))) {
            return switch (args.get(1).toLowerCase()) {
                case "years", "jahre" -> String.format("Es sind %f Jahr%s bis zu deinem Geburtstag.", birthdate.getYearsUntilBirthday(), birthdate.getYearsUntilBirthday() > 1 ? "e" : "");
                case "months", "monate" -> String.format("Es sind %f Monat%s bis zu deinem Geburtstag.", birthdate.getMonthsUntilBirthday(), birthdate.getMonthsUntilBirthday() > 1 ? "e" : "");
                case "weeks", "wochen" -> String.format("Es sind %f Woche%s bis zu deinem Geburtstag.", birthdate.getWeeksUntilBirthday(), birthdate.getWeeksUntilBirthday() > 1 ? "n" : "");
                case "hours", "stunden", "h" -> String.format("Es sind %d Stunde%s bis zu deinem Geburtstag.", birthdate.getHoursUntilBirthday(), birthdate.getHoursUntilBirthday() > 1 ? "n" : "");
                case "days", "tage", "d" -> String.format("Es sind %s Tag%s bis zu deinem Geburtstag.", birthdate.getDaysUntilBirthday(), birthdate.getDaysUntilBirthday() > 1 ? "e" : "");
                case "seconds", "sekunden", "s" -> String.format("Es sind %d Sekunde%s bis zu deinem Geburtstag.", birthdate.getSecondsUntilBirthday(), birthdate.getSecondsUntilBirthday() > 1 ? "n" : "");
                case "milliseconds", "millisekunden", "ms" -> String.format("Es sind %d Millisekunde%s bis zu deinem Geburtstag.", birthdate.getMillisecondsUntilBirthday(), birthdate.getMillisecondsUntilBirthday() > 1 ? "n" : "");
                default -> "Invalid Argument: " + syntax;
            };
        } else return "Dein Geburtstag ist am " + birthdate.getDate() + " YEPP";
    }

    private String nextBirthday(ArrayList<String> args) {

        // Variablen
        HashMap<Integer, Birthdate> birthdays = mySQL.getBirthdays();
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

    private String getThis(String verb) {
        // Variables
        HashMap<Integer, Birthdate> birthdays = mySQL.getBirthdays();
        List<Map.Entry<Integer, Birthdate>> sortedBirthdays = new ArrayList<>(birthdays.entrySet());
        HashSet<String> usernames = new HashSet<>();

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
                case "month", "monat" -> {
                    sortedBirthdays.removeIf(entry -> entry.getValue().getMonth() - 1 != currentMonth);
                    sortedBirthdays.sort(Comparator.comparingInt(entry -> entry.getValue().getDaysUntilBirthday()));
                    for (Map.Entry<Integer, Birthdate> entry : sortedBirthdays) {
                        usernames.add(helixHandler.getUser(entry.getKey()).getName());
                    }
                    if (usernames.isEmpty()) return "Niemand hat diesen Monat Geburtstag.";
                    else return "Folgende User haben diesen Monat Geburtstag: " + String.join(", ", usernames) + ". YEPP";
                }
                case "year", "jahr" -> {
                    sortedBirthdays.removeIf(entry -> entry.getValue().getDaysUntilBirthday() > (365 - today.get(Calendar.DAY_OF_YEAR)));
                    sortedBirthdays.sort(Comparator.comparingInt(entry -> entry.getValue().getDaysUntilBirthday()));
                    for (Map.Entry<Integer, Birthdate> entry : sortedBirthdays) {
                        usernames.add(helixHandler.getUser(entry.getKey()).getName());
                    }
                    if (usernames.isEmpty()) return "Niemand hat dieses Jahr Geburtstag.";
                    else return "Folgende User haben dieses Jahr Geburtstag: " + String.join(", ", usernames) + ". YEPP";
                }
                case "week", "woche" -> {
                    for (Map.Entry<Integer, Birthdate> entry : sortedBirthdays) {
                        Birthdate birthdate = entry.getValue();
                        Calendar birthdateCalendar = Calendar.getInstance(TIME_ZONE);
                        birthdateCalendar.set(Calendar.DAY_OF_MONTH, birthdate.getDay());
                        birthdateCalendar.set(Calendar.MONTH, birthdate.getMonth() - 1);
                        birthdateCalendar.set(Calendar.YEAR, currentYear);
                        if (birthdateCalendar.get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                            usernames.add(helixHandler.getUser(entry.getKey()).getName());
                        }
                    }
                    sortedBirthdays.sort(Comparator.comparingInt(entry -> entry.getValue().getDaysUntilBirthday()));
                    if (usernames.isEmpty()) return "Niemand hat diese Woche Geburtstag.";
                    else return "Folgende User haben diese Woche Geburtstag: " + String.join(", ", usernames) + ". YEPP";
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