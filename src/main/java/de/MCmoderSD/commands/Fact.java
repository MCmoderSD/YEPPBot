package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Format.*;

public class Fact {

    // Constructor
    public Fact(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "fact en/de";

        // About
        String[] name = {"fact", "fakt"};
        String description = "Sendet einen zufälligen Fakt. " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Determine language
                boolean isEnglish = false;
                if (!args.isEmpty()) isEnglish = args.getFirst().toLowerCase().startsWith("en");

                // Send message
                botClient.respond(event, getCommand(), trimMessage(mySQL.getAssetManager().getFact((isEnglish ? "en" : "de"))));
            }
        });
    }
}