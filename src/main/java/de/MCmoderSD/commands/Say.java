package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Say {

    // Constructor
    public Say(CommandHandler commandHandler, TwitchChat chat, String[] admins) {

        // Description
        String description = "Nur für Administratoren. Sendet eine Nachricht in den Chat. Verwendung: " + commandHandler.getPrefix() + "say <Nachricht>";


        // Register command
        commandHandler.registerCommand(new Command(description, "say", "repeat") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                if (Arrays.stream(admins).toList().contains(getAuthor(event).toLowerCase()) || getAuthor(event).equals(getChannel(event)))
                    chat.sendMessage(getChannel(event), String.join(" ", args));
            }
        });
    }
}