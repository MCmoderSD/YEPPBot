package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;
import de.MCmoderSD.utilities.database.MySQL;

public class Rank {

        // Constructor
    public Rank(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"rank", "rang", "stats"};
        String description = "Zeigt den Rank an. Verwendung: " + commandHandler.getPrefix() + "rank";


        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
            }
        });
    }
}