package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.enums.Scope;
import de.MCmoderSD.helix.handler.*;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class Moderate extends CommandBuilder {

    // Constructor
    public Moderate(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        String syntax = "Syntax: " + prefix + "moderate <join/leave|authenticate|block/unblock> [Befehl] [Benutzer]";

        // About
        String[] name = { "moderate", "mod", "moderrate", "modderate", "modderrate" };
        String description = "Ändert die Einstellungen des Bots. " + syntax;

        // Scope Array
        Scope[] scopes = new ArrayList<>(Arrays.asList(
                UserHandler.REQUIRED_SCOPES,
                ChatHandler.REQUIRED_SCOPES,
                RoleHandler.REQUIRED_SCOPES,
                StreamHandler.REQUIRED_SCOPES,
                ChannelHandler.REQUIRED_SCOPES
        ))
                .stream()
                .flatMap(Stream::of)
                .distinct()
                .toArray(Scope[]::new);


        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Variables
                var channel = event.getChannel();
                var user = event.getUser();

                // Check args
                if (args.isEmpty()) return twitchBot.sendMessage(event, name, syntax);

                // Get Action
                String action = args.getFirst().toLowerCase();
                if (!Arrays.asList("join", "leave", "block", "unblock", "authenticate", "auth", "oauth").contains(action)) return twitchBot.sendMessage(event, name, syntax);

                // Authenticate Action
                if (Arrays.asList("authenticate", "auth", "oauth").contains(action)) return twitchBot.sendMessage(event, name, "Authenticate here: " + helixHandler.getAuthorizationUrl(scopes));

                // Join/Leave Action
                if (Arrays.asList("join", "leave").contains(action)) {

                    if (args.size() == 1) switch (action) {
                        case "join": {
                            if (twitchBot.joinChannel(user)) return twitchBot.sendMessage(event, name, "Der Bot ist dem Kanal @" + user.getDisplayName() + " beigetreten.");
                            else return twitchBot.sendMessage(event, name, "Der Bot konnte dem Kanal @" + user.getDisplayName() + " nicht beitreten.");
                        }
                        case "leave": {
                            twitchBot.sendMessage(event, name, "Versuche, den Kanal @" + user.getDisplayName() + " zu verlassen...");
                            if (!twitchBot.leaveChannel(user)) return twitchBot.sendMessage(event, name, "Der Bot konnte den Kanal @" + user.getDisplayName() + " nicht verlassen.");
                            else return true;
                        }
                    }

                    // Parse Target Channel
                    String targetChannelName = args.get(1);
                    while (targetChannelName.startsWith("@")) targetChannelName = targetChannelName.substring(1);
                    TwitchUser targetChannel = userHandler.getTwitchUser(targetChannelName.toLowerCase());

                    // Validate Target Channel
                    if (targetChannel == null) return twitchBot.sendMessage(event, name, "Fehler: Kanal @" + targetChannelName + " nicht gefunden.");

                    // Check Permissions
                    if (!(user.equals(targetChannel) || twitchBot.isOwner(user))) return false;

                    // Perform Action
                    switch (action) {
                        case "join": {
                            if (twitchBot.joinChannel(targetChannel)) return twitchBot.sendMessage(event, name, "Der Bot ist dem Kanal @" + targetChannel.getDisplayName() + " beigetreten.");
                            else return twitchBot.sendMessage(event, name, "Der Bot konnte dem Kanal @" + targetChannel.getDisplayName() + " nicht beitreten.");
                        }
                        case "leave": {
                            twitchBot.sendMessage(event, name, "Versuche, den Kanal @" + targetChannel.getDisplayName() + " zu verlassen...");
                            if (!twitchBot.leaveChannel(targetChannel)) return twitchBot.sendMessage(event, name, "Der Bot konnte den Kanal @" + targetChannel.getDisplayName() + " nicht verlassen.");
                            else return true;
                        }
                    }
                }

                // Block/Unblock Action
                if (Arrays.asList("block", "unblock").contains(action)) {

                    // Check Args
                    if (args.size() < 2) return twitchBot.sendMessage(event, name, syntax);

                    // Get Command/Channel to Block/Unblock
                    String command = args.get(1).toLowerCase();

                    if (args.size() == 2) {

                        // Check Permissions
                        if (!twitchBot.isPermitted(user, channel)) return false;

                        // Perform Action
                        switch (action) {
                            case "block": {
                                if (commandHandler.blacklistAdd(channel, command)) return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' wurde zur Blacklist hinzugefügt.");
                                else return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' ist bereits auf der Blacklist oder existiert nicht.");
                            }
                            case "unblock": {
                                if (commandHandler.blacklistRemove(channel, command)) return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' wurde von der Blacklist entfernt.");
                                else return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' war nicht auf der Blacklist oder existiert nicht.");
                            }
                        }
                    }

                    // Parse Target Channel
                    String targetChannelName = args.get(2);
                    while (targetChannelName.startsWith("@")) targetChannelName = targetChannelName.substring(1);
                    TwitchUser targetChannel = userHandler.getTwitchUser(targetChannelName.toLowerCase());

                    // Validate Target Channel
                    if (targetChannel == null) return twitchBot.sendMessage(event, name, "Fehler: Kanal '" + targetChannelName + "' nicht gefunden.");

                    // Check Permissions
                    if (twitchBot.isPermitted(user, targetChannel)) return false;

                    // Perform Action
                    switch (action) {
                        case "block": {
                            if (commandHandler.blacklistAdd(targetChannel, command)) return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' wurde zur Blacklist von '" + targetChannel.getDisplayName() + "' hinzugefügt.");
                            else return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' ist bereits auf der Blacklist von '" + targetChannel.getDisplayName() + "' oder existiert nicht.");
                        }
                        case "unblock": {
                            if (commandHandler.blacklistRemove(targetChannel, command)) return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' wurde von der Blacklist von '" + targetChannel.getDisplayName() + "' entfernt.");
                            else return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' war nicht auf der Blacklist von '" + targetChannel.getDisplayName() + "' oder existiert nicht.");
                        }
                    }
                }

                // Send Message
                return twitchBot.sendMessage(event, name, syntax);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }
}