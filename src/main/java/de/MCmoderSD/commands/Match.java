package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import de.MCmoderSD.OpenAI.OpenAI;
import de.MCmoderSD.OpenAI.modules.Chat;
import de.MCmoderSD.utilities.other.Calculate;

import java.util.*;


import static de.MCmoderSD.utilities.other.Calculate.*;

public class Match {

    // Constants
    private final String noBirthdaySet;
    private final String mostCompatible;
    private final String and;
    private final String noCompatibleUsersFound;
    private final String thereforeYouAreMostCompatibleWith;

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
                HashMap<Integer, Birthdate> birthdayList = Calculate.getBirthdayList(event, botClient);
                if (birthdayList == null) {
                    botClient.respond(event, getCommand(), noBirthdaySet);
                    return;
                }
                Birthdate.ZodiacSign zodiacSign = birthdayList.get(event.getUserId()).getZodiacSign();
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

                // Response
                StringBuilder response = new StringBuilder(String.format("%s ", String.format(mostCompatible, zodiacSign.getTranslatedName())));

                // Get Compatible Signs and Users
                Birthdate.ZodiacSign[] compatibleSigns = zodiacSign.getCompatibleSigns();
                HashSet<Integer> mostCompatibleUsers = getCompatibleUsers(birthdayList, compatibleSigns[0], event.getUserId());
                HashSet<Integer> moreCompatibleUsers = getCompatibleUsers(birthdayList, compatibleSigns[1], event.getUserId());
                HashSet<Integer> compatibleUsers = getCompatibleUsers(birthdayList, compatibleSigns[2], event.getUserId());
                HashSet<Integer> combinedUsers = new HashSet<>(mostCompatibleUsers);

                // Add Compatible Signs
                response.append(compatibleSigns[0].getTranslatedName()).append(", ").append(compatibleSigns[1].getTranslatedName()).append(String.format(" %s ", and)).append(compatibleSigns[2].getTranslatedName()).append(".");

                // Trim Users
                if (mostCompatibleUsers.size() > amount) combinedUsers = stripRandomUser(mostCompatibleUsers, amount);
                else {
                    combinedUsers.addAll(stripRandomUser(moreCompatibleUsers, amount - combinedUsers.size()));
                    combinedUsers.addAll(stripRandomUser(compatibleUsers, amount - combinedUsers.size()));
                }

                // Check if no compatible users found
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

                System.out.println(combinedUsers);

                // Get Compatible Usernames
                HashSet<String> compatibleUserNames = new HashSet<>();
                helixHandler.getUsersByID(combinedUsers).forEach(user -> compatibleUserNames.add(user.getName()));
                System.out.println(compatibleUserNames);
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
    private HashSet<Integer> getCompatibleUsers(HashMap<Integer, Birthdate> birthdayList, Birthdate.ZodiacSign zodiacSign, int id) {
        HashSet<Integer> compatibleUsers = new HashSet<>();
        birthdayList.forEach((key, value) -> {
            if (value.getZodiacSign() == zodiacSign && key != id) compatibleUsers.add(key);
        });
        return compatibleUsers;
    }

    private HashSet<Integer> stripRandomUser(HashSet<Integer> compatibleUsers, int amount) {

        // Strip Random Users
        while (compatibleUsers.size() > amount) {
            var random = new Random().nextInt(compatibleUsers.size());
            compatibleUsers.remove((Integer) compatibleUsers.toArray()[random]);
        }

        return compatibleUsers;
    }
}