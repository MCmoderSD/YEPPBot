package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.objects.RaidEvent;
import de.MCmoderSD.tools.GZIP;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.MessageHelper.SPACE;

public class Shoutout extends CommandBuilder {

    // Constructor
    public Shoutout(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        String syntax = "Syntax: " + prefix + "so <enable | disable | @user> ";

        // About
        String[] name = {"shoutout", "so"};
        String description = "Enable or disable automatic shoutouts for raids. Use '@user' to shoutout a specific user.";


        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Get Variables
                TwitchUser channel = event.getChannel();
                TwitchUser user = event.getUser();
                boolean noArgs = args.isEmpty();

                // Check Permissions
                if (!twitchBot.isPermitted(event.getUser(), event.getChannel())) return false;

                // Last Raid
                RaidEvent lastRaid = getLastRaid(event.getChannel());

                // No Arguments
                if (noArgs && lastRaid == null) return twitchBot.sendMessage(event, name, "Fehler: Es gibt keinen letzten Raid, auf den ein Shoutout gemacht werden könnte. " + syntax);
                else if (noArgs) {
                    TwitchUser raider = lastRaid.getUser();         // Get Raider
                    streamHandler.sendShoutout(raider, channel);    // Send Shoutout
                    return true;
                }

                // Enable/Disable Auto Shoutout
                if (Arrays.asList("enable", "disable", "aktivieren", "deaktivieren").contains(args.getFirst().toLowerCase())) {

                    // Parse Argument
                    Boolean autoShoutout = switch (args.getFirst().toLowerCase()) {
                        case "enable", "aktivieren" -> true;
                        case "disable", "deaktivieren" -> false;
                        default -> null;
                    };

                    // Validate
                    if (autoShoutout == null) return twitchBot.sendMessage(event, name, "Fehler: Ungültiger Parameter. " + syntax);

                    // Update Channel Setting
                    channelManager.setAutoShoutout(channel, autoShoutout);

                    // Send Confirmation
                    return twitchBot.sendMessage(event, name, "Automatischer Shoutout für Raids wurde " + (autoShoutout ? "aktiviert." : "deaktiviert."));
                }

                // Specific User Shoutout
                if (args.getFirst().startsWith("@")) {

                    // Fetch Target User
                    String targetUserName = args.getFirst().substring(1).toLowerCase();
                    TwitchUser targetUser = userHandler.getTwitchUser(targetUserName);

                    // Validate Target User
                    if (targetUser == null) return twitchBot.sendMessage(event, name, "Fehler: Benutzer '" + targetUserName + "' nicht gefunden.");

                    // Send Shoutout
                    streamHandler.sendShoutout(targetUser, channel);
                    return true;
                }

                // Send Message
                return twitchBot.sendMessage(event, name, "Fehler: Ungültige Argumente." + SPACE + syntax);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    private RaidEvent getLastRaid(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

            // Fetch Last Raid
            PreparedStatement statement = database.getConnection().prepareStatement(
                    "SELECT event FROM RaidEvent WHERE channelId = ? ORDER BY firedAt DESC LIMIT 1"
            );

            // Set Parameters
            statement.setInt(1, channel.getId()); // Channel ID

            // Execute Query
            var resultSet = statement.executeQuery();

            // Process Result
            if (resultSet.next()) return (RaidEvent) GZIP.inflateObject(resultSet.getBytes("event"));
            else return null;

        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to fetch last raid event", e);
        }
    }
}