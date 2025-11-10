package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;

public class RoleSwap extends CommandBuilder {

    // Constructor
    public RoleSwap(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        String syntax = "Syntax: " + prefix + "say <Nachricht>";

        // About
        String[] name = { "RoleSwap", "RoleChange" };
        String description = "Nur für Moderatoren und Administratoren. Ändert die Rolle eines Benutzers. " + syntax;


        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Get Variables
                var user = event.getUser();
                var channel = event.getChannel();
                String targetUserName;
                String targetChannelName;
                TwitchUser targetUser;
                TwitchUser targetChannel;

                // Check Permissions
                if (!twitchBot.isOwner(user)) return false;

                // Check Args
                if (args.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Keine Rolle angegeben. " + syntax);
                if (args.size() > 2 && !args.get(2).isBlank()) targetChannelName = args.get(2);
                else targetChannelName = channel.getDisplayName();
                if (args.size() > 1 && !args.get(1).isBlank()) targetUserName = args.get(1);
                else targetUserName = user.getDisplayName();

                // Fetch Target Channel
                if (!(channel.getDisplayName().equals(targetChannelName) || targetChannelName.isBlank())) {
                    if (targetChannelName.startsWith("@")) targetChannelName = targetChannelName.substring(1);
                    targetChannel = userHandler.getTwitchUser(targetChannelName.toLowerCase());
                    if (targetChannel == null) return twitchBot.sendMessage(event, name, "Fehler: Kanal '" + targetChannelName + "' nicht gefunden.");
                } else targetChannel = channel;

                // Fetch Target User
                if (!(user.getDisplayName().equals(targetUserName) || targetUserName.isBlank())) {
                    if (targetUserName.startsWith("@")) targetUserName = targetUserName.substring(1);
                    targetUser = userHandler.getTwitchUser(targetUserName.toLowerCase());
                    if (targetUser == null) return twitchBot.sendMessage(event, name, "Fehler: Benutzer '" + targetUserName + "' nicht gefunden.");
                } else targetUser = user;

                // Get Role
                Role role = switch (args.getFirst().toLowerCase()) {
                    case "moderator", "mod" -> Role.MODERATOR;
                    case "vip" -> Role.VIP;
                    case "viewer", "user", "none" -> Role.VIEWER;
                    default -> null;
                };

                // Validate Role
                if (role == null) return twitchBot.sendMessage(event, name, "Fehler: Ungültige Rolle angegeben. " + syntax);

                // Change Role
                boolean success;
                try {
                    success = switch (role) {
                        case MODERATOR -> roleHandler.addModerator(targetUser, targetChannel);
                        case VIP -> roleHandler.addVIP(targetUser, targetChannel);
                        case VIEWER -> roleHandler.isModerator(targetUser, targetChannel) ? roleHandler.removeModerator(targetUser, targetChannel) : (!roleHandler.isVIP(targetUser, targetChannel) || roleHandler.removeVIP(targetUser, targetChannel));
                    };
                } catch (Exception e) {
                    success = false;
                }

                // Validate Success
                if (!success) return twitchBot.sendMessage(event, name, "Fehler: Konnte die Rolle nicht ändern.");

                // Send Message
                return twitchBot.sendMessage(event, name, "Die Rolle von " + targetUser.getDisplayName() + " wurde zu " + role.getName() + " geändert.");
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    private enum Role {

        // Values
        MODERATOR("Moderator"),
        VIP("VIP"),
        VIEWER("Viewer");

        // Attribute
        private final String name;

        // Constructor
        Role(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}