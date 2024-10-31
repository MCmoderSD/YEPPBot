package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;

import de.MCmoderSD.OpenAI.modules.Chat;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static de.MCmoderSD.utilities.other.Format.*;
import static de.MCmoderSD.utilities.other.Util.*;

public class Match {

    // Constants
    private final String noBirthdaySet;
    private final String mostCompatible;
    private final String and;
    private final String noCompatibleUsersFound;
    private final String thereforeYouAreMostCompatibleWith;

    // Constructor
    public Match(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, HelixHandler helixHandler, Chat chat) {

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
                LinkedHashMap<Integer, Birthdate> birthdays = removeNonFollower(event, mySQL.getBirthdays(), helixHandler);

                // Null Check
                if (birthdays == null) {
                    botClient.respond(event, getCommand(), noBirthdaySet);
                    return;
                }

                // Get Zodiac Sign and set Default Values
                Birthdate.ZodiacSign zodiacSign = birthdays.get(event.getUserId()).getZodiacSign();
                String language = "german"; // Default: German
                var amount = 5; // Default: 5

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

                // Get Compatible Signs and Users
                Birthdate.ZodiacSign[] compatibleSigns = zodiacSign.getCompatibleSigns();
                HashSet<Integer> mostCompatibleUsersIds = getCompatibleUserIds(birthdays, compatibleSigns[0], event.getUserId());
                HashSet<Integer> moreCompatibleUsersIds = getCompatibleUserIds(birthdays, compatibleSigns[1], event.getUserId());
                HashSet<Integer> compatibleUsersIds = getCompatibleUserIds(birthdays, compatibleSigns[2], event.getUserId());
                ArrayList<Integer> combinedUsersIds = new ArrayList<>(mostCompatibleUsersIds);

                // Trim Users
                if (mostCompatibleUsersIds.size() > amount) combinedUsersIds = stripRandomUserIds(mostCompatibleUsersIds, amount);
                else {
                    combinedUsersIds.addAll(stripRandomUserIds(moreCompatibleUsersIds, amount - combinedUsersIds.size()));
                    combinedUsersIds.addAll(stripRandomUserIds(compatibleUsersIds, amount - combinedUsersIds.size()));
                }

                // Build Response
                StringBuilder response = new StringBuilder(String.format("%s ", String.format(mostCompatible, zodiacSign.getTranslatedName())));
                response.append(compatibleSigns[0].getTranslatedName()).append(", ").append(compatibleSigns[1].getTranslatedName()).append(String.format(" %s ", and)).append(compatibleSigns[2].getTranslatedName()).append(".");

                // Check if no compatible users found
                if (combinedUsersIds.isEmpty()) response = new StringBuilder(noCompatibleUsersFound);
                else response.append(String.format(" %s", thereforeYouAreMostCompatibleWith));

                // Translate Response
                if (!Arrays.asList("de", "german", "deutsch", "ger").contains(language)) {
                    String instruction = trimMessage("Please translate the following text into " + language + ":");
                    response = new StringBuilder(chat.prompt(botClient.getBotName(), instruction, response.toString(), temperature, maxTokens, topP, frequencyPenalty, presencePenalty));
                }

                // Check if no compatible users found
                if (combinedUsersIds.isEmpty()) {
                    botClient.respond(event, getCommand(), response.toString());
                    return;
                }

                // Check Response Length
                var remainingChars = 500 - response.length() - 5;
                amount = Math.min(amount, remainingChars / 27);
                while (combinedUsersIds.size() > amount) combinedUsersIds.removeLast();

                // Get Compatible Users
                LinkedHashMap<Integer, String> compatibleUsers = new LinkedHashMap<>();
                for (Integer id : combinedUsersIds) //noinspection OptionalGetWithoutIsPresent
                    compatibleUsers.put(id, helixHandler.getUsersByID(new HashSet<>(combinedUsersIds)).stream().filter(user -> user.getId() == id).findFirst().get().getName());

                // Final Response
                StringBuilder finalResponse = response;
                finalResponse.append(" ");
                compatibleUsers.forEach((key, value) -> finalResponse.append(value).append(", "));
                finalResponse.deleteCharAt(finalResponse.length() - 2);
                finalResponse.append(" YEPP");

                // Send Response
                botClient.respond(event, getCommand(), response.toString());
            }
        });
    }

    // Get Compatible User IDs
    private HashSet<Integer> getCompatibleUserIds(HashMap<Integer, Birthdate> birthdayList, Birthdate.ZodiacSign zodiacSign, int id) {

        // Variables
        HashSet<Integer> compatibleUserIds = new HashSet<>();

        // Get Compatible Users
        birthdayList.forEach((key, value) -> {
            if (value.getZodiacSign() == zodiacSign && key != id) compatibleUserIds.add(key);
        });

        // Return
        return compatibleUserIds;
    }

    // Strip Random User
    private ArrayList<Integer> stripRandomUserIds(HashSet<Integer> compatibleUserIds, int amount) {

        // Check if amount is 0
        if (amount == 0) return new ArrayList<>();

        // Variables
        ArrayList<Integer> compatibleUserIdList = new ArrayList<>(compatibleUserIds);

        // Check if amount is bigger than list
        if (amount > compatibleUserIds.size()) return compatibleUserIdList;

        // Remove Random Users
        while (compatibleUserIdList.size() > amount) compatibleUserIdList.remove(RANDOM.nextInt(compatibleUserIdList.size()));

        // Return
        return compatibleUserIdList;
    }
}