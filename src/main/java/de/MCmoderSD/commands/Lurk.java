package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Lurk {

    // Constructor
    public Lurk(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, HashMap<String, String> lurkChannel, HashMap<String, Long> lurkTime) {

        // About
        String[] name = {"lurk", "lürk", "afk"}; // Command name and aliases
        String description = "Sendet den Befehl " + commandHandler.getPrefix() + "lurk in den Chat, um im Lurk zu sein";

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String author = getAuthor(event);
                String channel = getChannel(event);

                // Send message
                // chat.sendMessage(channel, author + " ist jetzt im Lurk!"); ToDo Temporary disabled

                // Save data
                lurkChannel.put(author, channel);
                lurkTime.put(author, System.currentTimeMillis());
            }
        });
    }
}