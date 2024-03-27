package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Key {

    // Constructor
    public Key(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // Syntax
        String syntax = "Syntax " + commandHandler.getPrefix() + "key <Spiel>.";

        // About
        String[] name = {"key", "buy", "instant", "instant-gaming"};
        String description = "Zeigt dir wo du das Spiel am Günstigen bekommst. " + syntax;

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Check arguments
                String response;
                if (args.length == 0) response = "Du musst ein Spiel angeben. " + syntax;
                else {
                    for (var i = 0; i < args.length; i++) args[i] = args[i].replace("?", "");
                    String game = String.join("+", trimMessage(processArgs(args)));
                    while (game.endsWith("+")) game = game.trim();
                    String url = "https://www.instant-gaming.com/de/suche/?igr=moder?q=" + game;
                    response = "Hier bekommst du das Spiel am Günstigsten: " + url;
                }

                // Send message
                chat.sendMessage(getChannel(event), response);

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }
}