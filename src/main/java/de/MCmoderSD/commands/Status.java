package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.util.ArrayList;

public class Status {

    // Constructor
    public Status(BotClient botClient, MessageHandler messageHandler) {

        // About
        String[] name = {"status", "test"};
        String description = "Zeigt den Status des Bots an. Also ob er aktiv ist oder nicht.";


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Send Message
                botClient.respond(event, getCommand(), "Bot ist aktiv!");
            }
        });
    }
}