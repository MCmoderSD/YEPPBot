package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.api.ProkeralaAPI;
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
    public Horoscope(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, HelixHandler helixHandler, Chat chat, JsonNode apiconfig) {

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

        // Get Chat Config
        JsonNode config = chat.getConfig();

        // Get Parameters
        double temperature = 0;
        int maxTokens = config.get("maxTokens").asInt();
        double topP = config.get("topP").asDouble();
        double frequencyPenalty = config.get("frequencyPenalty").asDouble();
        double presencePenalty = config.get("presencePenalty").asDouble();


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
                    } else if (!(arg.isEmpty() || arg.isBlank())) language = arg.toLowerCase();
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
                if (dailyPrediction.isEmpty() || dailyPrediction.isBlank()) {
                    botClient.respond(event, getCommand(), errorGettingHoroscope);
                    return;
                }

                // Reset API Swaps
                apiSwaps = 0;

                // Translate Horoscope
                if (Arrays.asList("en", "english", "englisch", "eng").contains(language)) botClient.respond(event, getCommand(), dailyPrediction);
                else {
                    String instruction = trimMessage("Please translate the following text into " + language + ":");
                    String translatedPrediction = chat.prompt(botClient.getBotName(), instruction, dailyPrediction, temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
                    botClient.respond(event, getCommand(), translatedPrediction);
                }
            }
        });
    }

    // Get Daily Prediction
    private String getDailyPrediction(Birthdate birthdate) {
        String dailyPrediction = prokeralaAPI.dailyPrediction(birthdate);
        if (dailyPrediction.equals("Failed to connect to the API. Response code: 403")) initAPI();
        if (apiSwaps >= clientIDs.length) return "Failed to connect to the API. Please try again later.";
        else if (dailyPrediction.equals("Failed to connect to the API. Response code: 403")) {
            apiSwaps++;
            return getDailyPrediction(birthdate);
        } else return dailyPrediction;
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