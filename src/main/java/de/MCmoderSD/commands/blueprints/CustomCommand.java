package de.MCmoderSD.commands.blueprints;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CustomCommand extends CommandBuilder {

    public CustomCommand(TwitchBot twitchBot, TwitchUser channel, ResultSet resultSet) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");
        if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
        if (resultSet == null) throw new IllegalArgumentException("ResultSet cannot be null");

        // Call Super
        super(twitchBot);

        try {

            // Get Command Attributes
            var name = resultSet.getString("name");
            var alias = resultSet.getString("alias").split(",");
            var response = resultSet.getString("message");
            var type = Type.fromString(resultSet.getString("type"));
            var userLevel = UserLevel.fromString(resultSet.getString("userLevel"));

            // Create Command Identifier
            var identifier = new String[]{ name };
            if (alias.length > 0 && !alias[0].isBlank()) {
                var temp = new String[alias.length + 1];
                temp[0] = name;
                System.arraycopy(alias, 0, temp, 1, alias.length);
                identifier = temp;
            }

            // Create Command
            var command = new Command("This is a Custom Command", identifier) {

                @Override
                public boolean execute(MessageEvent event, ArrayList<String> args) {

                    // Check Permissions
                    switch (userLevel) {
                        case FOLLOWER -> {
                            if (!twitchBot.isFollower(event.getUser(), event.getChannel())) return false;
                        }
                        case VIP -> {
                            if (!twitchBot.isVIP(event.getUser(), event.getChannel())) return false;
                        }
                        case EDITOR -> {
                            if (!twitchBot.isEditor(event.getUser(), event.getChannel())) return false;
                        }
                        case MODERATOR -> {
                            if (!twitchBot.isModerator(event.getUser(), event.getChannel())) return false;
                        }
                        case BROADCASTER -> {
                            if (!twitchBot.isBroadcaster(event.getUser(), event.getChannel())) return false;
                        }
                    }

                    // Send Message
                    return twitchBot.sendMessage(event, name, response);
                }
            };

            // Register Command
            var success = commandHandler.registerCustomCommand(channel, command);
            if (!success) System.err.println("Failed to register custom command: " + name + " for channel: " + channel.getDisplayName());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load command from database", e);
        }
    }

    enum Type {

        // Command Types
        REPLY, MENTION, SAY;

        // Methods
        public static Type fromString(String type) {
            return switch (type.toLowerCase()) {
                case "reply" -> REPLY;
                case "mention" -> MENTION;
                case "say" -> SAY;
                default -> throw new IllegalArgumentException("Invalid command type: " + type);
            };
        }

        public static Type fromInt(int type) {
            return switch (type) {
                case 0 -> REPLY;
                case 1 -> MENTION;
                case 2 -> SAY;
                default -> throw new IllegalArgumentException("Invalid command type: " + type);
            };
        }
    }

    enum UserLevel {

        // User Levels
        EVERYONE, FOLLOWER, VIP, EDITOR, MODERATOR, BROADCASTER;

        // Methods
        public static UserLevel fromString(String level) {
            return switch (level.toLowerCase()) {
                case "everyone" -> EVERYONE;
                case "follower" -> FOLLOWER;
                case "vip" -> VIP;
                case "editor" -> EDITOR;
                case "moderator" -> MODERATOR;
                case "broadcaster" -> BROADCASTER;
                default -> throw new IllegalArgumentException("Invalid user level: " + level);
            };
        }

        public static UserLevel fromInt(int level) {
            return switch (level) {
                case 0 -> EVERYONE;
                case 1 -> FOLLOWER;
                case 2 -> VIP;
                case 3 -> EDITOR;
                case 4 -> MODERATOR;
                case 5 -> BROADCASTER;
                default -> throw new IllegalArgumentException("Invalid user level: " + level);
            };
        }
    }
}