package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class Rank {

    // Constructor
    public Rank(BotClient botClient, MessageHandler messageHandler) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "rank <valo|lol|siege|apex>";

        // About
        String[] name = {"rank", "rang"};
        String description = "Zeigt den Rank des Streamers an:" + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                if (args.size() > 1 && Arrays.asList("set", "update").contains(args[1]))
            }
        });
    }
}