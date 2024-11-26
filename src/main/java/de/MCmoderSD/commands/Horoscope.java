package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.astrology.ProkeralaAPI;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
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

    // Credentials
    private final String[] clientIDs;
    private final String[] clientSecrets;

    // Attributes
    private ProkeralaAPI prokeralaAPI;
    private int apiIndex = 0;
    private int apiSwaps = 0;

    // Constructor
    public Horoscope(BotClient botClient, MessageHandler messageHandler, HelixHandler helixHandler, MySQL mySQL, Chat chat, JsonNode apiconfig) {

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
        JsonNode prokeralaConfig = apiconfig.get("astrology");
        clientIDs = prokeralaConfig.get("clientId").asText().split(", ");
        clientSecrets = prokeralaConfig.get("clientSecret").asText().split(", ");
        initAPI();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Variables
                HashMap<Integer, Birthdate> birthdayList = mySQL.getBirthdays();
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
                String dailyPrediction = getDailyPrediction(birthdate);
                if (dailyPrediction.isBlank()) {
                    botClient.respond(event, getCommand(), errorGettingHoroscope);
                    return;
                }

                // Reset API Swaps
                apiSwaps = 0;

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

    // Get Daily Prediction
    private String getDailyPrediction(Birthdate birthdate) {

        // Get Daily Prediction
        String dailyPrediction = prokeralaAPI.dailyPrediction(birthdate.getMonthDay());

        // Check for API Error
        if (dailyPrediction.startsWith("Error: ")) {

            // Check for API Swap
            var code = Integer.parseInt(dailyPrediction.substring(7));
            if (code == 403) initAPI();

            // Retry
            if (apiSwaps >= clientIDs.length) return "Failed to connect to the API. Please try again later.";
            else {
                apiSwaps++;
                return getDailyPrediction(birthdate);
            }
        }

        return dailyPrediction;
    }

    // Load Horoscope List
    private void initAPI() {

        // Swap API
        if (prokeralaAPI != null) {
            apiIndex++;
            if (apiIndex >= clientIDs.length) apiIndex = 0;
        }

        // Initialize API
        prokeralaAPI = new ProkeralaAPI(clientIDs[apiIndex], clientSecrets[apiIndex]);
    }
}