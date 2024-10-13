package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.openAI.OpenAI;
import de.MCmoderSD.utilities.openAI.modules.Chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;


import static de.MCmoderSD.utilities.other.Calculate.*;

public class Match {

    // Attributes
    private final HashMap<String, LinkedHashMap<String, String>> matchMap;

    // Constructor
    public Match(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, HelixHandler helixHandler, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "match ";

        // About
        String[] name = {"match", "matching"};
        String description = "Zeigt dir welcher User am besten zu dir passt. " + syntax;

        // Load Sternzeichen
        matchMap = loadMatchList("/assets/matchList.json");

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
                String language = "de"; // Default: German
                String zodiacSign = birthdayList.get(event.getUserId()).getTranslatedZodiacSign();

                // Check Zodiac Sign
                if (zodiacSign == null || zodiacSign.isEmpty() || zodiacSign.isBlank()) {
                    botClient.respond(event, getCommand(), "Du hast noch kein Geburtsdatum angegeben. Syntax: " + botClient.getPrefix() + "birthdate <Tag.Monat.Jahr>");
                    return;
                }

                // Response
                StringBuilder response = new StringBuilder("Am kompatibelsten mit deinem Sternzeichen " + zodiacSign + " sind: ");
                HashSet<String> compatibleSigns = new HashSet<>();
                LinkedHashMap<String, String> matches = Objects.requireNonNull(matchMap).get(zodiacSign);

                matches.entrySet().stream().limit(3).forEach(e -> {
                    compatibleSigns.add(e.getKey());
                    response.append(e.getKey()).append(", ");
                });

                 // Get compatible Users
                HashSet<Integer> compatibleUsers = new HashSet<>();
                birthdayList.forEach((id, birthdate) -> {
                    if (compatibleSigns.contains(birthdate.getTranslatedZodiacSign()) && id != event.getUserId()) compatibleUsers.add(id);
                });

                // Get Usernames
                HashSet<String> compatibleUserNames = new HashSet<>();
                compatibleUsers.forEach(id -> compatibleUserNames.add(helixHandler.getUser(id).getName()));

                 // Replace last comma
                response.replace(response.length() - 2, response.length(), ". ");
                if (compatibleUserNames.isEmpty()) response.append("Leider gibt es keine kompatiblen User.");
                else response.append("Die kompatibelsten User sind: ").append(String.join(", ", compatibleUserNames)).append(".");

                // Send Response
                botClient.respond(event, getCommand(), response.toString());
            }
        });
    }

    private void getCompatibleSigns(Birthdate birthdate) {

        String zodiacSign = birthdate.getTranslatedZodiacSign();
        String[] compatibleSigns = matchMap.get(zodiacSign).keySet().toArray(String[]::new);


    }


    @SuppressWarnings("SameParameterValue")
    private HashMap<String, LinkedHashMap<String, String>> loadMatchList(String path) {
        try {

            // Load the file as an InputStream
            InputStream inputStream = Match.class.getResourceAsStream(path);
            if (inputStream == null) throw new IOException("The file " + path + " could not be found.");

            // Read the contents of the file as a String
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) jsonBuilder.append(line);
            reader.close();

            // Create a new Gson object and Type
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, LinkedHashMap<String, String>>>() {}.getType();

            // Parse JSON into a Map
            return gson.fromJson(jsonBuilder.toString(), type);

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            return null;
        }
    }
}