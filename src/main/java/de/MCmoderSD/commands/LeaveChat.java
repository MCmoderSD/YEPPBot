package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class LeaveChat {
    // Constructor
    public LeaveChat(CommandHandler commandHandler, TwitchChat chat, String[] admins) {

        // Description
        String description = "Entfernt den Bot aus einem Chat. Nur der Broadcaster und Admins können diesen Befehl verwenden. Verwendung: " + commandHandler.getPrefix() + "leavechat <Channel>";


        // Register command
        commandHandler.registerCommand(new Command(description, "leavechat", "removechat", "removefromchat", "delfromchat") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String channel = getChannel(event);

                if (getAuthor(event).equals(channel)) leave(event, chat, channel); // Broadcaster
                else if (Arrays.stream(admins).toList().contains(getAuthor(event).toLowerCase())) leave(event, chat, args[0]); // Admin
            }
        });
    }

    // Leave chat
    private void leave(ChannelMessageEvent event, TwitchChat chat, String... args) {
        chat.sendMessage(getChannel(event), "Leaving " + args[0]);
        chat.leaveChannel(args[0]);
    }
}