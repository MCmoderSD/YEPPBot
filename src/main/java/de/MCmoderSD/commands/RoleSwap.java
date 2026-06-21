package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.MessageHelper.tagUser;

public class RoleSwap extends CommandBuilder {

    // Constructor
    public RoleSwap(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        var syntax = "Syntax: " + prefix + "RoleSwap <Rolle> [Benutzer] [Kanal]";

        // About
        var name = new String[]{ "RoleSwap", "RoleChange", "rs" };
        var description = "Nur für Moderatoren und Administratoren. Ändert die Rolle eines Benutzers. " + syntax;


        // Register command
        var registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Get Variables
                var argsSize = args.size();
                var user = event.getUser();
                var channel = event.getChannel();
                String targetUserName;
                String targetChannelName;
                TwitchUser targetUser;
                TwitchUser targetChannel;

                // Check Permissions
                if (!twitchBot.isPermitted(user, channel)) return false;

                // Check Args
                if (args.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Keine Rolle angegeben. " + syntax);

                // Parse Target Channel
                if (argsSize > 2) {

                    // Check Permissions
                    if (!twitchBot.isOwner(user)) return false;

                    // Parse Name
                    targetChannelName = args.get(2);
                    while (targetChannelName.startsWith("@")) targetChannelName = targetChannelName.substring(1);

                    // Fetch Target Channel
                    targetChannel = userHandler.getTwitchUser(targetChannelName.toLowerCase());
                    if (targetChannel == null) return twitchBot.sendMessage(event, name, "Fehler: Kanal '" + targetChannelName + "' nicht gefunden. YEPP");

                } else targetChannel = channel;

                // Parse Target User
                if (argsSize > 1) {

                    // Parse Name
                    targetUserName = args.get(1);
                    while (targetUserName.startsWith("@")) targetUserName = targetUserName.substring(1);

                    // Fetch Target User
                    targetUser = userHandler.getTwitchUser(targetUserName.toLowerCase());
                    if (targetUser == null) return twitchBot.sendMessage(event, name, "Fehler: Benutzer '" + targetUserName + "' nicht gefunden. YEPP");

                } else targetUser = user;

                // Get Role
                var role = switch (args.getFirst().toLowerCase()) {
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
                if (!success) return twitchBot.sendMessage(event, name, "Fehler: Konnte die Rolle nicht ändern. YEPP");

                // Send Message
                return twitchBot.sendMessage(event, name, "Die Rolle von " + tagUser(targetUser) + " wurde zu " + role.getName() + " geändert. YEPP");
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    // Role Enum
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

        // Getter
        public String getName() {
            return name;
        }
    }
}