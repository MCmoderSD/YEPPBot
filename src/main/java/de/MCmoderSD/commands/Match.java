package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.data.Birthdate;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.openai.services.ChatService;

import java.util.*;

import static com.openai.models.ReasoningEffort.*;
import static de.MCmoderSD.openai.models.ChatModel.*;
import static de.MCmoderSD.utilities.MessageHelper.*;

public class Match extends CommandBuilder {

    // Constructor
    public Match(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        String syntax = "Syntax: " + prefix + "Match <amount> <language>";

        // About
        String[] name = { "Match", "Matching" };
        String description = "Interagiere mit ChatGPT! Sende eine Nachricht, um eine Antwort zu erhalten, oder verwende 'reset', um die Konversation zurückzusetzen. " + syntax;

        // Check if OpenAI is configured
        if (openAI == null) return;

        // Initialize ChatService
        ChatService service = ChatService.builder()
                .setModel(GPT_5_5)
                .setReasoningEffort(NONE)
                .setMaxOutputTokens(120)
                .setInstructions(
                        ""
                )
                .build(openAI);

        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Variables
                var user = event.getUser();
                var channel = event.getChannel();
                var birthdays = getBirthdays(channel);

                // Check if channel has any birthdays
                if (getBirthdays(channel).isEmpty()) return twitchBot.sendMessage(event, name, "In diesem Kanal wurden noch keine Geburtstage gesetzt. YEPP");

                // Check Birthdate
                if (!birthdays.containsKey(user)) return twitchBot.sendMessage(event, name, tagUser(user) + ", du hast kein Geburtsdatum hinterlegt. Benutze '" + prefix + "Birthday set <Geburtsdatum>', um dein Geburtsdatum zu hinterlegen.");

                return false;
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    // Get Birthdays of Channel Community
    private HashMap<TwitchUser, Birthdate> getBirthdays(TwitchUser channel) {

        // Variables
        HashMap<TwitchUser, Birthdate> birthdays = birthdayManager.getBirthdays();

        // Get Users
        HashSet<TwitchUser> users = new HashSet<>();
        users.addAll(roleHandler.getSubscribers(channel));  // Subscribers
        users.addAll(roleHandler.getModerators(channel));   // Moderators
        users.addAll(roleHandler.getFollowers(channel));    // Followers
        users.addAll(chatHandler.getChatters(channel));     // Chatters
        users.addAll(roleHandler.getEditors(channel));      // Editors
        users.addAll(roleHandler.getVIPs(channel));         // VIPs
        users.add(channel);                                 // Channel Owner

        // Map Birthdays
        HashMap<TwitchUser, Birthdate> birthdayMap = new HashMap<>();
        for (var user : users) {
            user = new TwitchUser(user);
            var birthdate = birthdays.get(user);
            if (birthdate != null) birthdayMap.put(user, birthdate);
        }

        // Return Birthdays
        return birthdayMap;
    }
}