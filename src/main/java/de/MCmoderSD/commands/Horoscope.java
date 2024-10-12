package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.main.Credentials;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.api.ProkeralaAPI;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.openAI.OpenAI;
import de.MCmoderSD.utilities.openAI.modules.Chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Horoscope {

    // Constructor
    public Horoscope(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, Credentials credentials, HelixHandler helixHandler, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "horoscope @<user> <language>";

        // About
        String[] name = {"horoscope", "horoskop", "horoskope"};
        String description = "Zeigt dein Horoskop an. " + syntax;

        // Initialize ProkeralaAPI
        JsonNode prokeralaConfig = credentials.getAPIConfig().get("astrology");
        ProkeralaAPI prokeralaAPI = new ProkeralaAPI(prokeralaConfig.get("clientId").asText(), prokeralaConfig.get("clientSecret").asText());

        // Get Chat Module and Config
        Chat chat = openAI.getChat();
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
                String language = "de";             // Default: German
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
                    if (hasTarget) botClient.respond(event, getCommand(), "Dieser Nutzer hat kein Geburtsdatum.");
                    else {
                        botClient.respond(event, getCommand(), "Du hast kein Geburtsdatum angegeben.");
                        return;
                    }
                }

                // Check Birthdate
                if (birthdate == null) {
                    botClient.respond(event, getCommand(), "Fehler beim Abrufen des Geburtsdatums.");
                    return;
                }

                // Get Horoscope
                String dailyPrediction = prokeralaAPI.dailyPrediction(birthdate);
                if (dailyPrediction == null || dailyPrediction.isEmpty() || dailyPrediction.isBlank()) {
                    botClient.respond(event, getCommand(), "Fehler beim Abrufen des Horoskops.");
                    return;
                }

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
}