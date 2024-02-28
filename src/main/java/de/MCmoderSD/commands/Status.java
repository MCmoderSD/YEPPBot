package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;

import static de.MCmoderSD.utilities.Calculate.getChannel;

public class Status {

    // Constructor
    public Status(CommandHandler commandHandler, TwitchChat chat) {

        // Register command
        commandHandler.registerCommand(new Command("status", "test") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                chat.sendMessage(getChannel(event), "Bot ist aktiv!");
            }
        });
    }
}