package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.ChannelManager;

import java.util.ArrayList;
import java.util.Arrays;

public class Moderate {

    // Associations
    private final BotClient botClient;
    private final MessageHandler messageHandler;
    private final ChannelManager channelManager;

    // Constructor
    public Moderate(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // Init Associations
        this.botClient = botClient;
        this.messageHandler = messageHandler;
        this.channelManager = mySQL.getChannelManager();

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "moderate join/leave/block/unblock command/channel";

        // About
        String[] name = {"moderate", "mod", "moderrate", "modderate", "modderrate"};
        String description = "Ändert die Einstellungen des Bots. " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Variables
                String channel = event.getChannel();
                String user = event.getUser();
                String response = syntax;

                // Check args
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                String verb = args.getFirst().toLowerCase();
                if (!Arrays.asList("join", "leave", "block", "unblock").contains(verb)) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                if (Arrays.asList("block", "unblock").contains(verb) && args.size() < 2) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Response
                switch (verb) {
                    case "join":
                        if (args.size() < 2) response = join(user); // Broadcaster
                        else if (botClient.isModerator(event)) response = join(channel); // Moderator
                        else if ( botClient.isAdmin(event)) response = join(args.get(1)); // Admin
                        break;
                    case "leave":
                        if (args.size() < 2) response = leave(user); // Broadcaster
                        else if (botClient.isModerator(event)) response = leave(channel); // Moderator
                        else if (botClient.isAdmin(event)) response = leave(args.get(1)); // Admin
                        break;
                    case "block":
                        if (botClient.isBroadcaster(event)) response = editBlacklist(user, args.get(1), true); // Broadcaster
                        else if (botClient.isModerator(event)) response = editBlacklist(channel, args.get(1), true); // Moderator
                        else if (botClient.isAdmin(event) &&  args.size() == 2) response = editBlacklist(channel, args.get(1), true); // Admin
                        else if (botClient.isAdmin(event) &&  args.size() == 3) response = editBlacklist(args.get(2), args.get(1), true); // Admin
                        break;
                    case "unblock":
                        if (botClient.isBroadcaster(event)) response = editBlacklist(user, args.get(1), false); // Broadcaster
                        else if (botClient.isModerator(event)) response = editBlacklist(channel, args.get(1), false); // Moderator
                        else if (botClient.isAdmin(event) &&  args.size() == 2) response = editBlacklist(channel, args.get(1), false); // Admin
                        else if (botClient.isAdmin(event) &&  args.size() == 3) response = editBlacklist(args.get(2), args.get(1), false); // Admin
                }

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }

    // Join chat
    private String join(String channel) {
        if (channel.startsWith("@")) channel = channel.substring(1);
        channel = channel.toLowerCase();
        botClient.joinChannel(channel);
        return channelManager.editChannel(channel, true);
    }

    // Leave chat
    private String leave(String channel) {
        if (channel.startsWith("@")) channel = channel.substring(1);
        channel = channel.toLowerCase();
        botClient.leaveChannel(channel);
        return channelManager.editChannel(channel, false);
    }

    // Edit blacklist
    private String editBlacklist(String channel, String command, boolean block) {

        // Format
        channel = channel.startsWith("@") ? channel.substring(1) : channel;
        channel = channel.toLowerCase();
        command = command.toLowerCase();

        if (channel.isEmpty() || channel.isBlank()) return "Error: Channel is empty!";

        // Check command
        command = messageHandler.checkCommand(command) ? command : messageHandler.getAliasList().getOrDefault(command, null);
        if (command == null) return "Der Befehl existiert nicht!";

        // Edit blacklist
        String response = channelManager.editBlacklist(channel, command, block);
        messageHandler.updateBlackList(channelManager.getBlackList());
        return response;
    }
}