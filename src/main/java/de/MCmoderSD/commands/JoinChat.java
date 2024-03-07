package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import java.util.Arrays;

import static de.MCmoderSD.utilities.Calculate.*;

public class JoinChat {

    // Constructor
    public JoinChat(CommandHandler commandHandler, TwitchChat chat, String[] admins) {

        // Description
        String description = "Fügt den Bot einem Chat hinzu. Nur der Broadcaster und Admins können diesen Befehl verwenden. Verwendung: " + commandHandler.getPrefix() + "joinchat <Channel>";


        // Register command
        commandHandler.registerCommand(new Command(description, "joinchat", "addchat", "addtochat") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                if (getAuthor(event).equals(args[0])) join(event, chat, args[0]); // Broadcaster
                else if (Arrays.stream(admins).toList().contains(getAuthor(event).toLowerCase())) join(event, chat, args[0]); // Admin
            }
        });
    }

    // Join chat
    private void join(ChannelMessageEvent event, TwitchChat chat, String... args) {
        chat.sendMessage(getChannel(event), "Joining " + args[0]);
        chat.joinChannel(args[0]);
    }
}