package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;

public class Ping extends CommandBuilder {

    // Constructor
    public Ping(TwitchBot twitchBot) {
        super(twitchBot);

        // About
        String[] name = { "ping", "latency" };
        String description = "Sendet eine Nachricht mit der Latenz des Bots zurück.";


        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Send Message
                return twitchBot.sendMessage(event, name, "Pong " + twitchBot.getChat().getLatency() + "ms");
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }
}