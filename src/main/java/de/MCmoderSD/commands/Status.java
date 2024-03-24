package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Status {

    // Constructor
    public Status(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"status", "test"};
        String description = "Zeigt den Status des Bots an. Also ob er aktiv ist oder nicht.";


        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                // Report status
                String response = "Bot ist aktiv!";
                chat.sendMessage(getChannel(event), response);
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }
}