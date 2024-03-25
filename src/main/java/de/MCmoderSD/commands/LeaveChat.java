package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class LeaveChat {

    // Constructor
    public LeaveChat(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, ArrayList<String> admins) {

        // About
        String[] name = {"leavechat", "removechat", "removefromchat", "delfromchat"};
        String description = "Entfernt den Bot aus einem Chat. Nur der Broadcaster und Admins können diesen Befehl verwenden. Verwendung: " + commandHandler.getPrefix() + "leavechat <Channel>";


        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String channel = getChannel(event);
                String author = getAuthor(event);

                String response;
                if (author.equals(channel)) response = leave(chat, channel); // Broadcaster
                else if (admins.contains(author)) response = leave(chat, args[0]); // Admin
                else return;

                // Send Message
                chat.sendMessage(channel, response);

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }

    // Leave chat
    private String leave(TwitchChat chat, String... args) {
        chat.leaveChannel(args[0]);
        return "Leaving " + args[0];
    }
}