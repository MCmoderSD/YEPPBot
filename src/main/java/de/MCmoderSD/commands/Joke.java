package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Joke {

    // Constructor
    public Joke(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // Syntax
        String syntax = "Syntax: " + commandHandler.getPrefix() + "joke en/de";

        // About
        String[] name = {"joke", "witz"};
        String description = "Sendet einen zufälligen Witz. " + syntax;

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Determine language
                boolean isEnglish = false;
                if (args.length > 0) isEnglish = args[0].toLowerCase().startsWith("en");

                // Send message
                String response = trimMessage(mySQL.getJoke(isEnglish ? "en" : "de"));
                chat.sendMessage(getChannel(event), response);

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }
}