package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

public class Rank {

        // Constructor
    public Rank(CommandHandler commandHandler, TwitchChat chat) {

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