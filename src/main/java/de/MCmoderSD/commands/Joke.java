package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Joke {

    // Constructor
    public Joke(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "joke en/de";

        // About
        String[] name = {"joke", "witz"};
        String description = "Sendet einen zufälligen Witz. " + syntax;

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Determine language
                boolean isEnglish = false;
                if (!args.isEmpty()) isEnglish = args.getFirst().toLowerCase().startsWith("en");

                // Send message
                botClient.respond(event, getCommand(), trimMessage(mySQL.getAssetManager().getJoke(isEnglish ? "en" : "de")));
            }
        });
    }
}