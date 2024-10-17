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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;


import static de.MCmoderSD.utilities.other.Calculate.*;

public class Match {

    // Constants
    private final String noBirthdaySet;
    private final String mostCompatible;
    private final String and;
    private final String noCompatibleUsersFound;
    private final String thereforeYouAreMostCompatibleWith;

    // Attributes
    private final HashMap<String, LinkedHashMap<String, String>> matchMap;

    // Constructor
    public Match(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, HelixHandler helixHandler, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "match <amount> <language>";

        // About
        String[] name = {"match", "matching"};
        String description = "Zeigt dir welcher User am besten zu dir passt. " + syntax;

        // Constants
        noBirthdaySet = "Du hast noch kein Geburtsdatum angegeben.";
        mostCompatible = "Am kompatibelsten mit deinem Sternzeichen %s sind";
        and = "und";
        noCompatibleUsersFound = "Es gibt keine kompatiblen User.";
        thereforeYouAreMostCompatibleWith = "Demnach bist du am kompatibelsten mit";

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
                if (birthdayList == null) {
                    botClient.respond(event, getCommand(), noBirthdaySet);
                    return;
                }
                String zodiacSign = birthdayList.get(event.getUserId()).getTranslatedZodiacSign();
                String language = "german"; // Default: German
                var amount = 5; // Default: 5

                // Remove all non followers
                birthdayList.keySet().removeIf(user -> !helixHandler.isFollower(event.getChannelId(), user));

                // Check Zodiac Sign
                if (zodiacSign == null || zodiacSign.isEmpty() || zodiacSign.isBlank()) {
                    botClient.respond(event, getCommand(), String.format("%s %s", noBirthdaySet, syntax));
                    return;
                }

                // Check Args
                if (!args.isEmpty()) {
                    try {
                        amount = Integer.parseInt(args.getFirst());
                    } catch (NumberFormatException e) {
                        language = args.getFirst();
                    }
                }

                if (args.size() > 1) {
                    try {
                        amount = Integer.parseInt(args.getFirst());
                        language = args.get(1);
                    } catch (NumberFormatException e) {
                        language = args.get(1);
                    }
                }

                // Response
                StringBuilder response = new StringBuilder(String.format("%s ", String.format(mostCompatible, zodiacSign)));

                // Get Compatible Signs and Users
                String[] compatibleSigns = Objects.requireNonNull(matchMap).get(zodiacSign).keySet().toArray(String[]::new);
                HashSet<Integer> mostCompatibleUsers = getCompatibleUsers(birthdayList, compatibleSigns[0], event.getUserId());
                HashSet<Integer> moreCompatibleUsers = getCompatibleUsers(birthdayList, compatibleSigns[1], event.getUserId());
                HashSet<Integer> compatibleUsers = getCompatibleUsers(birthdayList, compatibleSigns[2], event.getUserId());
                HashSet<Integer> combinedUsers = new HashSet<>();

                // Add Compatible Signs
                response.append(compatibleSigns[0]).append(", ").append(compatibleSigns[1]).append(String.format(" %s ", and)).append(compatibleSigns[2]).append(".");

                // Trim Users
                if (mostCompatibleUsers.size() > amount) combinedUsers = pickRandomUsers(mostCompatibleUsers, amount);
                if (combinedUsers.size() < amount && !moreCompatibleUsers.isEmpty()) combinedUsers.addAll(pickRandomUsers(moreCompatibleUsers, amount - combinedUsers.size()));
                if (combinedUsers.size() < amount && !compatibleUsers.isEmpty()) pickRandomUsers(compatibleUsers, amount - combinedUsers.size());

                // Check if there are any compatible Users
                if (combinedUsers.isEmpty()) response = new StringBuilder(noCompatibleUsersFound);
                else response.append(String.format(" %s", thereforeYouAreMostCompatibleWith));

                // Translate Text
                if (!Arrays.asList("de", "german", "deutsch", "ger").contains(language)) {
                    String instruction = trimMessage("Please translate the following text into " + language + ":");
                    response = new StringBuilder(chat.prompt(botClient.getBotName(), instruction, response.toString(), temperature, maxTokens, topP, frequencyPenalty, presencePenalty));
                }

                // Send Response
                if (combinedUsers.isEmpty()) {
                    botClient.respond(event, getCommand(), response.toString());
                    return;
                }

                // Check Length
                var remainingChars = 500 - response.length() - 5;
                amount = Math.min(amount, remainingChars / 27);
                while (combinedUsers.size() > amount) combinedUsers.remove((Integer) combinedUsers.toArray()[new Random().nextInt(combinedUsers.size())]);

                // Get Compatible Usernames
                HashSet<String> compatibleUserNames = new HashSet<>();
                compatibleUsers.forEach(id -> compatibleUserNames.add(helixHandler.getUser(id).getName()));
                StringBuilder finalResponse = response;
                finalResponse.append(" ");
                compatibleUserNames.forEach(name -> finalResponse.append(name).append(", "));
                finalResponse.deleteCharAt(finalResponse.length() - 2);
                finalResponse.append(" YEPP");

                // Send Response
                botClient.respond(event, getCommand(), response.toString());
            }
        });
    }

    // Get Compatible User IDs
    private HashSet<Integer> getCompatibleUsers(HashMap<Integer, Birthdate> birthdayList, String zodiacSign, int id) {
        HashSet<Integer> compatibleUsers = new HashSet<>();
        for (Map.Entry<Integer, Birthdate> entry : birthdayList.entrySet()) if (entry.getValue().getTranslatedZodiacSign().equals(zodiacSign) && entry.getKey() != id) compatibleUsers.add(entry.getKey());
        return compatibleUsers;
    }

    private HashSet<Integer> pickRandomUsers(HashSet<Integer> compatibleUsers, int amount) {
        while (compatibleUsers.size() > amount) compatibleUsers.remove(compatibleUsers.size() - 1);
        return compatibleUsers;
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