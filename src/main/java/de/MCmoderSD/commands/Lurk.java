package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;

import java.util.HashMap;

import static de.MCmoderSD.utilities.Calculate.getAuthor;
import static de.MCmoderSD.utilities.Calculate.getChannel;

public class Lurk {

    // Constructor
    public Lurk(CommandHandler commandHandler, TwitchChat chat, HashMap<String, String> lurkChannel, HashMap<String, Long> lurkTime) {

        // Register command
        commandHandler.registerCommand(new Command("lurk", "lürk") { // Command name and aliases
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