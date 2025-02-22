package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.enums.ZodiacSign;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.OpenAI.modules.Chat;
import de.MCmoderSD.utilities.database.SQL;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.ArrayList;
import java.util.HashMap;

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
    public Match(BotClient botClient, MessageHandler messageHandler, HelixHandler helixHandler, SQL sql, Chat chat) {

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

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Variables
                LinkedHashMap<Integer, Birthdate> birthdays = removeNonFollower(event, sql.getBirthdays(), helixHandler);

                // Null Check
                if (birthdays == null) {
                    botClient.respond(event, getCommand(), "The bot is not authorized to read the followers of this channel. Type !mod auth to authorize the bot. YEPP");
                    return;
                }

                // Check if empty
                if (birthdays.isEmpty()) {
                    botClient.respond(event, getCommand(), noBirthdaySet);
                    return;
                }

                // Get Zodiac Sign and set Default Values
                ZodiacSign zodiacSign = birthdays.get(event.getUserId()).getZodiacSign();
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
                ZodiacSign[] compatibleSigns = zodiacSign.getCompatibleSigns();
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
                    response = new StringBuilder(chat.prompt(null, instruction, response.toString(), 0d, null, null, null, null));
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
                    compatibleUsers.put(id, helixHandler.getUsersByID(new HashSet<>(combinedUsersIds)).stream().filter(user -> Objects.equals(user.getId(), id)).findFirst().get().getName());

                // Final Response
                StringBuilder finalResponse = response;
                finalResponse.append(SPACE);
                compatibleUsers.forEach((key, value) -> finalResponse.append(value).append(", "));
                finalResponse.deleteCharAt(finalResponse.length() - 2);
                finalResponse.append(" YEPP");

                // Send Response
                botClient.respond(event, getCommand(), response.toString());
            }
        });
    }

    // Get Compatible User IDs
    private HashSet<Integer> getCompatibleUserIds(HashMap<Integer, Birthdate> birthdayList, ZodiacSign zodiacSign, int id) {

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