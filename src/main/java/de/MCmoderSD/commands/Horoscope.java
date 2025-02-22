package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.astrology.manager.Astrology;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.SQL;
import de.MCmoderSD.OpenAI.modules.Chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Format.*;

public class Horoscope {

    // Constants
    private final String noBirthdaySet;
    private final String youHaveNoBirthdaySet;
    private final String errorGettingBirthday;
    private final String errorGettingHoroscope;

    // Constructor
    public Horoscope(BotClient botClient, MessageHandler messageHandler, HelixHandler helixHandler, SQL sql, Chat chat, JsonNode apiconfig) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "horoscope @<user> <language>";

        // About
        String[] name = {"horoscope", "horoscop", "horoskop", "horoskope"};
        String description = "Zeigt dein Horoskop an. " + syntax;

        // Constants
        noBirthdaySet = "Dieser Nutzer hat kein Geburtsdatum.";
        youHaveNoBirthdaySet = "Du hast kein Geburtsdatum angegeben.";
        errorGettingBirthday = "Fehler beim Abrufen des Geburtsdatums.";
        errorGettingHoroscope = "Fehler beim Abrufen des Horoskops.";

        // Load ProkeralaAPI Credentials
        JsonNode astrologyApiConfig = apiconfig.get("astrology");
        Astrology api = new Astrology(astrologyApiConfig.get("clientId").asText().split(", "), astrologyApiConfig.get("clientSecret").asText().split(", "));

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Variables
                HashMap<Integer, Birthdate> birthdayList = sql.getBirthdays();
                String language = "german";         // Default: German
                var targetID = event.getUserId();   // Default: User
                boolean hasTarget = false;
                Birthdate birthdate = null;

                // Check Args
                for (String arg : args) {
                    if (arg.startsWith("@")) {
                        targetID = helixHandler.getUser(arg.substring(1).toLowerCase()).getId();
                        hasTarget = true;
                    } else if (!(arg.isBlank())) language = arg.toLowerCase();
                }

                // Check Target
                if (birthdayList.containsKey(targetID)) birthdate = birthdayList.get(targetID);
                else {
                    if (hasTarget) botClient.respond(event, getCommand(), noBirthdaySet);
                    else {
                        botClient.respond(event, getCommand(), youHaveNoBirthdaySet);
                        return;
                    }
                }

                // Check Birthdate
                if (birthdate == null) {
                    botClient.respond(event, getCommand(), errorGettingBirthday);
                    return;
                }

                // Get Horoscope
                String dailyPrediction = api.dailyPrediction(birthdate.getMonthDay()).getPrediction();
                if (dailyPrediction.isBlank()) {
                    botClient.respond(event, getCommand(), errorGettingHoroscope);
                    return;
                }

                // Translate Horoscope
                if (Arrays.asList("en", "english", "englisch", "eng").contains(language)) botClient.respond(event, getCommand(), dailyPrediction);
                else {
                    String instruction = trimMessage("Please translate the following text into " + language + ":");
                    String translatedPrediction = chat.prompt(null, instruction, dailyPrediction, 0d, null, null, null, null);
                    botClient.respond(event, getCommand(), translatedPrediction);
                }
            }
        });
    }
}