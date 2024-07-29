package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Moderate {

    // Associations
    private final MySQL mySQL;
    private final CommandHandler commandHandler;


    // Constructor
    public Moderate(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, ArrayList<String> admins) {

        // Init Associations
        this.mySQL = mySQL;
        this.commandHandler = commandHandler;

        // Syntax
        String syntax = "Syntax: " + commandHandler.getPrefix() + "moderate join/leave/block/unblock command/channel";

        // About
        String[] name = {"moderate","mod"};
        String description = "Ändert die Einstellungen des Bots. " + syntax;


        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String channel = getChannel(event);
                String author = getAuthor(event);

                // Check args
                String response = getDescription();
                if (args.length < 1) {
                    sendMessage(chat, event, getCommand(), channel, response, args);
                    return;
                }

                String verb = args[0].toLowerCase();
                if (!Arrays.asList("join", "leave", "block", "unblock").contains(verb)) {
                    sendMessage(chat, event, getCommand(), channel, response, args);
                    return;
                }

                if (Arrays.asList("block", "unblock").contains(verb) && args.length < 2) {
                    sendMessage(chat, event, getCommand(), channel, response, args);
                    return;
                }

                // Response
                switch (verb) {
                    case "join":
                        if (args.length < 2) response = join(chat, author); // Broadcaster
                        else if (admins.contains(author)) response = join(chat, args[1].toLowerCase()); // Admin
                        break;
                    case "leave":
                        if (args.length < 2) response = leave(chat, author); // Broadcaster
                        else if (admins.contains(author)) response = leave(chat, args[1].toLowerCase()); // Admin
                        break;
                    case "block":
                        if (args.length < 3) response = editBlacklist(author, args[1].toLowerCase(), true); // Broadcaster
                        else if (admins.contains(author)) response = editBlacklist(args[2], args[1].toLowerCase(), true); // Admin
                        break;
                    case "unblock":
                        if (args.length < 3) response = editBlacklist(author, args[1].toLowerCase(), false); // Broadcaster
                        else if (admins.contains(author)) response = editBlacklist(args[2], args[1].toLowerCase(), false); // Admin
                        break;
                    default:
                        sendMessage(chat, event, getCommand(), channel, response, args);
                        return;
                }

                // Send message
                sendMessage(chat, event, getCommand(), channel, response, args);
            }
        });
    }

    // Send Message
    private void sendMessage(TwitchChat chat, ChannelMessageEvent event, String command, String channel, String response, String... args) {

        // Send Message
        chat.sendMessage(channel, response);

        // Log response
        mySQL.logResponse(event, command, processArgs(args), response);
    }

    // Leave chat
    private String leave(TwitchChat chat, String channel) {
        chat.leaveChannel(channel);
        return mySQL.editChannel(channel, false);
    }

    // Join chat
    private String join(TwitchChat chat, String channel) {
        chat.joinChannel(channel);
        return mySQL.editChannel(channel, true);
    }

    private String editBlacklist(String channel, String command, boolean block) {
        command = commandHandler.getCommands().containsKey(command) ? command : commandHandler.getAliases().getOrDefault(command, null);
        if (command == null) return "Der Befehl existiert nicht!";
        String response = mySQL.editBlacklist(channel, command, block);
        commandHandler.updateBlackList();
        return response;
    }
}