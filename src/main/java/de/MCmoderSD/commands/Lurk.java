package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;
import de.MCmoderSD.core.InteractionHandler;

import de.MCmoderSD.utilities.database.MySQL;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Lurk {

    // Constructor
    @SuppressWarnings("unused")
    public Lurk(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, InteractionHandler interactionHandler) {

        // About
        String[] name = {"lurk", "lürk", "afk"}; // Command name and aliases
        String description = "Sendet den Befehl " + commandHandler.getPrefix() + "lurk in den Chat, um im Lurk zu sein";

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String author = getAuthor(event);
                String channel = getChannel(event);

                // Save data
                mySQL.saveLurk(event, getTimestamp(), interactionHandler);

                // Send message
                String response = author + " ist jetzt im Lurk!";
                // chat.sendMessage(channel, response); ToDo Temporary disabled

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }
}