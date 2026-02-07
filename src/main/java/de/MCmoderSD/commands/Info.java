package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.MessageHelper.tagUser;

public class Info extends CommandBuilder {

    // Constructor
    public Info(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        String syntax = "Syntax: " + prefix + "Info <mod|editor|vip>";

        // About
        String[] name = new String[]{ "Info", "Information" };
        String description = "Zeigt Informationen über einen Kanal an, wie z.B. Moderatoren, Editoren oder VIPs. " + syntax;


        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Check Permissions
                if (!twitchBot.isPermitted(event.getUser(), event.getChannel())) return false;

                // Check Arguments
                if (args.isEmpty()) return twitchBot.sendMessage(event, name, "Bitte gib eine Rolle an. " + syntax);

                // Parse Role
                String role = args.getFirst().toLowerCase();

                // Variables
                TwitchUser targetChannel = event.getChannel();

                // Parse Target Channel
                if (args.size() > 1) {

                    // Check Permissions
                    if (!twitchBot.isOwner(event.getUser())) return false;

                    // Parse Name
                    String targetChannelName = args.get(1);
                    while (targetChannelName.startsWith("@")) targetChannelName = targetChannelName.substring(1);

                    // Fetch Target Channel
                    TwitchUser fetchedChannel = userHandler.getTwitchUser(targetChannelName.toLowerCase());
                    if (fetchedChannel == null) return twitchBot.sendMessage(event, name, "Fehler: Kanal '" + targetChannelName + "' nicht gefunden. YEPP");
                    targetChannel = fetchedChannel;
                }

                // Perform Action
                switch (role) {

                    // Moderators
                    case "mod", "mods", "moderator" -> {

                        // Get Mods
                        var mods = roleHandler.getModerators(targetChannel);

                        // Send Message
                        if (mods.isEmpty()) return twitchBot.sendMessage(event, name, "Der Kanal " + tagUser(targetChannel) + " hat keine Moderatoren.");

                        // Build Response
                        StringBuilder response = new StringBuilder(tagUser(targetChannel) + String.format(" hat %d Moderatoren: ", mods.size()));
                        for (var mod : mods) response.append(tagUser(mod)).append(", ");
                        response.setLength(response.length() - 2); // Remove last comma and space

                        // Send Message
                        return twitchBot.sendMessage(event, name, response.toString());
                    }

                    // Editors
                    case "editor", "editors" -> {

                        // Get Editors
                        var editors = roleHandler.getEditors(targetChannel);

                        // Send Message
                        if (editors.isEmpty()) return twitchBot.sendMessage(event, name, "Der Kanal " + tagUser(targetChannel) + " hat keine Editoren.");

                        // Build Response
                        StringBuilder response = new StringBuilder(tagUser(targetChannel) + String.format(" hat %d Editoren: ", editors.size()));
                        for (var editor : editors) response.append(tagUser(editor)).append(", ");
                        response.setLength(response.length() - 2); // Remove last comma and space

                        // Send Message
                        return twitchBot.sendMessage(event, name, response.toString());
                    }

                    // VIPs
                    case "vip", "vips" -> {

                        // Get VIPs
                        var vips = roleHandler.getVIPs(targetChannel);

                        // Send Message
                        if (vips.isEmpty()) return twitchBot.sendMessage(event, name, "Der Kanal " + tagUser(targetChannel) + " hat keine VIPs.");

                        // Build Response
                        StringBuilder response = new StringBuilder(tagUser(targetChannel) + String.format(" hat %d VIPs: ", vips.size()));
                        for (var vip : vips) response.append(tagUser(vip)).append(", ");
                        response.setLength(response.length() - 2); // Remove last comma and space

                        // Send Message
                        return twitchBot.sendMessage(event, name, response.toString());
                    }

                    // Invalid Role
                    default -> {
                        return twitchBot.sendMessage(event, name, "Fehler: Ungültige Rolle angegeben. " + syntax);
                    }
                }
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }
}