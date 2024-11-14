package de.MCmoderSD.commands;

import com.github.twitch4j.chat.events.channel.RaidEvent;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.EventHandler;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.ChannelManager;

import java.util.ArrayList;
import java.util.Arrays;

public class Shoutout {

    // Associations
    private final BotClient botClient;
    private final HelixHandler helixHandler;
    private final ChannelManager channelManager;

    // Constructor
    public Shoutout(BotClient botClient, MessageHandler messageHandler, EventHandler eventHandler, HelixHandler helixHandler, MySQL mySQL) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "so <enable | disable | @user> ";

        // About
        String[] name = {"shoutout", "so"};
        String description = "Enable or disable automatic shoutouts for raids. Use '@user' to shoutout a specific user.";

        // Set Associations
        this.botClient = botClient;
        this.helixHandler = helixHandler;
        this.channelManager = mySQL.getChannelManager();

        // Response
        String userNotFound = "User not found.";
        String error = "No raid found.";
        String failed = "Failed to send shoutout.";

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                var test = eventHandler.getLastRaid();
                test.forEach((key, value) -> System.out.println(key + " " + value));

                // Check if user is moderator or admin
                if (!(botClient.isAdmin(event) || botClient.isPermitted(event))) return;

                // Last Raider
                if (args.isEmpty()) {

                    // Get Last Raider
                    RaidEvent raidEvent = eventHandler.getLastRaid(event.getChannelId());

                    // Check if raid exists
                    if (raidEvent == null) {
                        botClient.respond(event, getCommand(), error);
                        return;
                    }

                    // Send Shoutout
                    boolean success = helixHandler.sendShoutout(event.getChannelId(), new TwitchUser(raidEvent.getRaider()));

                    // Send Message
                    if (success) return;
                    botClient.respond(event, getCommand(), failed);
                    return;
                }

                if (Arrays.asList("enable", "disable", "aktivieren", "deaktivieren").contains(args.getFirst().toLowerCase())) {
                    String response = switch (args.getFirst().toLowerCase()) {
                        case "enable", "aktivieren" -> autoShoutout(event, args, true);
                        case "disable", "deaktivieren" -> autoShoutout(event, args, false);
                        default -> syntax;
                    };

                    botClient.respond(event, getCommand(), response);
                    return;
                }

                // Specific User
                String user = args.getFirst().toLowerCase();
                while (user.startsWith("@")) user = user.substring(1);
                TwitchUser twitchUser = helixHandler.getUser(user);

                // Check if user exists
                if (twitchUser == null) {
                    botClient.respond(event, getCommand(), userNotFound);
                    return;
                }

                // Send Shoutout
                boolean success = helixHandler.sendShoutout(event.getChannelId(), twitchUser);

                // Send Message
                if (success) return;
                botClient.respond(event, getCommand(), failed);
            }
        });
    }

    // Auto Shoutout
    private String autoShoutout(TwitchMessageEvent event, ArrayList<String> args, boolean isAutoShoutout) {

        Integer id = event.getChannelId();
        if (args.size() > 1 && botClient.isAdmin(event)) {
            String taggedChannel = args.get(1).toLowerCase();
            while (taggedChannel.startsWith("@")) taggedChannel = taggedChannel.substring(1);
            id = helixHandler.getUser(taggedChannel).getId();
        }

        return channelManager.autoShoutout(id, isAutoShoutout);
    }
}