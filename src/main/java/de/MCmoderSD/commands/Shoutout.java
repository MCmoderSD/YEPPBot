package de.MCmoderSD.commands;

import com.github.twitch4j.chat.events.channel.RaidEvent;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.EventHandler;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;

import java.util.ArrayList;

public class Shoutout {

    // Constructor
    public Shoutout(BotClient botClient, MessageHandler messageHandler, EventHandler eventHandler, HelixHandler helixHandler) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "so @user";

        // About
        String[] name = {"shoutout", "so"};
        String description = "Macht ein shoutout an entweder den letzten Raider oder an einen spezifischen User. " + syntax;

        // Response
        String error = "Der User konnte nicht gefunden werden.";
        String failed = "Der Shoutout konnte nicht gesendet werden. Versuche erst den Bot zu authentifizieren: " + botClient.getPrefix() + "mod auth";

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check if user is moderator or admin
                if (!(botClient.isAdmin(event) || botClient.isPermitted(event))) return;

                // Last Raider
                if (args.isEmpty()) {

                    // Get Last Raider
                    RaidEvent raidEvent = eventHandler.getLastRaid(event.getChannelId());

                    // Check if raid exists
                    if (raidEvent == null) {
                        botClient.respond(event, getCommand(), error);
                        return;
                    }
                    // Send Shoutout
                    boolean success = helixHandler.sendShoutout(event, new TwitchUser(raidEvent.getRaider()));

                    // Send Message
                    if (success) return;
                    botClient.respond(event, getCommand(), failed);
                    return;
                }

                // Specific User
                String user = args.getFirst().toLowerCase();
                while (user.startsWith("@")) user = user.substring(1);
                TwitchUser twitchUser = helixHandler.getUser(user);

                // Check if user exists
                if (twitchUser == null) {
                    botClient.respond(event, getCommand(), error);
                    return;
                }

                // Send Shoutout
                boolean success = helixHandler.sendShoutout(event, twitchUser);

                // Send Message
                if (success) return;
                botClient.respond(event, getCommand(), failed);
            }
        });
    }
}