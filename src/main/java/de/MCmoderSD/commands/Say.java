package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.util.ArrayList;

public class Say {

    // Constructor
    public Say(BotClient botClient, MessageHandler messageHandler) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "say <Nachricht>";

        // About
        String[] name = {"say", "repeat"};
        String description = "Nur für Moderatoren und Administratoren. Sendet eine Nachricht in den Chat. " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check if user is moderator or admin
                if (!(botClient.isModerator(event) || botClient.isAdmin(event.getUser()))) return;

                // Send Message
                botClient.respond(event, getCommand(), String.join(" ", args));
            }
        });
    }
}