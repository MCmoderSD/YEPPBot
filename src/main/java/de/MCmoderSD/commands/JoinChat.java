package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class JoinChat {

    // Constructor
    public JoinChat(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, ArrayList<String> admins) {

        // About
        String[] name = {"joinchat", "addchat", "addtochat"};
        String description = "Fügt den Bot einem Chat hinzu. Nur der Broadcaster und Admins können diesen Befehl verwenden. Verwendung: " + commandHandler.getPrefix() + "joinchat <Channel>";


        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String channel = getChannel(event);
                String author = getAuthor(event);

                String response;
                if (author.equals(args[0])) response = join(chat, args[0]); // Broadcaster
                else if (admins.contains(author)) response = join(chat, args[0]); // Admin
                else return;

                // Send Message
                chat.sendMessage(channel, response);

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }

    // Join chat
    private String join(TwitchChat chat, String... args) {
        chat.joinChannel(args[0]);
        return "Joining " + args[0];
    }
}