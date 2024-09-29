package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import javax.management.InvalidAttributeValueException;
import java.util.ArrayList;
import java.util.HashMap;

public class Birthday {

    // Association
    private final BotClient botClient;
    private final MySQL mySQL;
    // Attributes
    private final String syntax;

    // Constructor
    public Birthday(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // Syntax
        syntax = "Syntax: " + botClient.getPrefix() + "birthday set <DD.MM.CCYY> / get <days|hours|seconds>";

        // About
        String[] name = {"birthday", "bday", "geburtstag"};
        String description = "Setzt deinen Geburtstag. " + syntax;

        // Association
        this.botClient = botClient;
        this.mySQL = mySQL;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check Arguments
                if (args.size() > 1) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                String verb = args.getFirst().toLowerCase();

                switch (verb) {
                    case "set":
                        botClient.respond(event, getCommand(), setBirthday(event, args));
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

        // Check Birthday
        String[] date = args.get(1).split("\\.");
        if (date.length != 3) return syntax;

        // Set Birthday
        Birthdate birthdate;
        try {
            birthdate = new Birthdate(args.get(1));
        } catch (InvalidAttributeValueException | NumberFormatException e) {
            return "Invalid Date: " + date[0] + "." + date[1] + "." + date[2];
        }

        // Set Birthday
        mySQL.setBirthday(event, birthdate);
        botClient.getMessageHandler().updateBirthdateList(mySQL.getBirthdays());

        // Response
        return "Dein Geburtstag wurde auf " + date[0] + "." + date[1] + "." + date[2] + " gesetzt.";
    }

    private String getBirthday(TwitchMessageEvent event, ArrayList<String> args) {

        // Variables
        HashMap<Integer, Birthdate> birthdays = mySQL.getBirthdays();
        var id = event.getUserId();

        // Check Birthday
        if (!birthdays.containsKey(id)) return "Du hast noch keinen Geburtstag gesetzt.";

        // Get Birthday
        Birthdate birthdate = birthdays.get(id);

        // Response
        return switch (args.get(1).toLowerCase()) {
            case "years", "jahre" -> String.format("Es sind %f Jahr%c bis zu deinem Geburtstag.", birthdate.getYearsUntilBirthday(), birthdate.getYearsUntilBirthday() > 1 ? 'e' : null);
            case "months", "monate" -> String.format("Es sind %f Monat%c bis zu deinem Geburtstag.", birthdate.getMonthsUntilBirthday(), birthdate.getMonthsUntilBirthday() > 1 ? 'e' : null);
            case "weeks", "wochen" -> String.format("Es sind %f Woche%c bis zu deinem Geburtstag.", birthdate.getWeeksUntilBirthday(), birthdate.getWeeksUntilBirthday() > 1 ? 'n' : null);
            case "days", "tage", "d" -> String.format("Es sind %s Tag%c bis zu deinem Geburtstag.", birthdate.getDaysUntilBirthday(), birthdate.getDaysUntilBirthday() > 1 ? 'e' : null);
            case "hours", "stunden", "h" -> String.format("Es sind %d Stunde%c bis zu deinem Geburtstag.", birthdate.getHoursUntilBirthday(), birthdate.getHoursUntilBirthday() > 1 ? 'n' : null);
            case "seconds", "sekunden", "s" -> String.format("Es sind %d Sekunde%c bis zu deinem Geburtstag.", birthdate.getSecondsUntilBirthday(), birthdate.getSecondsUntilBirthday() > 1 ? 'n' : null);
            case "milliseconds", "millisekunden", "ms" -> String.format("Es sind %d Millisekunde%c bis zu deinem Geburtstag.", birthdate.getMillisecondsUntilBirthday(), birthdate.getMillisecondsUntilBirthday() > 1 ? 'n' : null);
            default -> "Dein Geburtstag ist am " + birthdate.getDay() + "." + birthdate.getMonth() + "." + birthdate.getYear();
        };
    }
}