package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;

public class Status extends CommandBuilder {

    private final String botIsActive;

    // Constructor
    public Status(TwitchBot twitchBot) {
        super(twitchBot);


        // About
        String[] name = {"status", "test"};
        String description = "Zeigt den Status des Bots an. Also ob er aktiv ist oder nicht.";

        // Constants
        botIsActive = "Bot ist aktiv!";


        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Send Message
                return twitchBot.sendMessage(event, name, botIsActive);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }
}