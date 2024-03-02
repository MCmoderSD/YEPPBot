package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import java.util.Arrays;

import static de.MCmoderSD.utilities.Calculate.*;

public class Say {

    // Constructor
    public Say(CommandHandler commandHandler, TwitchChat chat, String[] admins) {

        // Register command
        commandHandler.registerCommand(new Command("say", "repeat") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                if (Arrays.stream(admins).toList().contains(getAuthor(event).toLowerCase()) || getAuthor(event).equals(getChannel(event)))
                    chat.sendMessage(getChannel(event), String.join(" ", args));
            }
        });
    }
}