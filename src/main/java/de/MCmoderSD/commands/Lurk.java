package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;

import java.util.HashMap;

import static de.MCmoderSD.utilities.Calculate.getAuthor;

public class Lurk {

    // Constructor
    public Lurk(CommandHandler commandHandler, TwitchChat chat, HashMap<String, String> lurkChannel, HashMap<String, Long> lurkTime) {

        // Register command
        commandHandler.registerCommand(new Command("lurk", "lürk") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Send message
                chat.sendMessage(event.getChannel().getName(), getAuthor(event) + " ist jetzt im Lurk!");

                // Save data
                lurkChannel.put(getAuthor(event), event.getChannel().getName());
                lurkTime.put(event.getUser().getName(), System.currentTimeMillis());
            }
        });
    }
}
