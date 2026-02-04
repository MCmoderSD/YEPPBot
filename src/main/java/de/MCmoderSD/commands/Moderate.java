package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.enums.Scope;
import de.MCmoderSD.helix.handler.ChannelHandler;
import de.MCmoderSD.helix.handler.ChatHandler;
import de.MCmoderSD.helix.handler.RoleHandler;
import de.MCmoderSD.helix.handler.StreamHandler;
import de.MCmoderSD.helix.handler.UserHandler;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static de.MCmoderSD.utilities.MessageHelper.tagUser;

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
                var argsSize = args.size();

                // Check args
                if (args.isEmpty()) return twitchBot.sendMessage(event, name, syntax);

                // Get Action
                String action = args.getFirst().toLowerCase();
                if (!Arrays.asList("join", "leave", "block", "unblock", "authenticate", "auth", "oauth").contains(action)) return twitchBot.sendMessage(event, name, syntax);

                // Authenticate Action
                if (Arrays.asList("authenticate", "auth", "oauth").contains(action)) return twitchBot.sendMessage(event, name, "Authenticate here: " + helixHandler.getAuthorizationUrl(scopes));

                // Join/Leave Action
                if (Arrays.asList("join", "leave").contains(action)) {

                    if (argsSize == 1) switch (action) {
                        case "join": {
                            if (twitchBot.joinChannel(user)) return twitchBot.sendMessage(event, name, "Der Bot ist dem Kanal " + tagUser(user) + " beigetreten. YEPP");
                            else return twitchBot.sendMessage(event, name, "Der Bot konnte dem Kanal " + tagUser(user) + " nicht beitreten. YEPP");
                        }
                        case "leave": {
                            twitchBot.sendMessage(event, name, "Versuche, den Kanal " + tagUser(user) + " zu verlassen...");
                            if (!twitchBot.leaveChannel(user)) return twitchBot.sendMessage(event, name, "Der Bot konnte den Kanal " + tagUser(user) + " nicht verlassen. YEPP");
                            else return true;
                        }
                    }

                    // Parse Target Channel
                    String targetChannelName = args.get(1);
                    while (targetChannelName.startsWith("@")) targetChannelName = targetChannelName.substring(1);
                    TwitchUser targetChannel = userHandler.getTwitchUser(targetChannelName.toLowerCase());

                    // Validate Target Channel
                    if (targetChannel == null) return twitchBot.sendMessage(event, name, "Fehler: Kanal @" + targetChannelName + " nicht gefunden. YEPP");

                    // Check Permissions
                    if (!(user.equals(targetChannel) || twitchBot.isOwner(user))) return false;

                    // Perform Action
                    switch (action) {
                        case "join": {
                            if (twitchBot.joinChannel(targetChannel)) return twitchBot.sendMessage(event, name, "Der Bot ist dem Kanal " + tagUser(targetChannel) + " beigetreten. YEPP");
                            else return twitchBot.sendMessage(event, name, "Der Bot konnte dem Kanal " + tagUser(targetChannel) + " nicht beitreten. YEPP");
                        }
                        case "leave": {
                            twitchBot.sendMessage(event, name, "Versuche, den Kanal " + tagUser(targetChannel) + " zu verlassen...");
                            if (!twitchBot.leaveChannel(targetChannel)) return twitchBot.sendMessage(event, name, "Der Bot konnte den Kanal " + tagUser(targetChannel) + " nicht verlassen. YEPP");
                            else return true;
                        }
                    }
                }

                // Block/Unblock Action
                if (Arrays.asList("block", "unblock").contains(action)) {

                    // Check Args
                    if (argsSize < 2) return twitchBot.sendMessage(event, name, syntax);

                    // Get Command/Channel to Block/Unblock
                    String command = args.get(1).toLowerCase();

                    if (argsSize == 2) {

                        // Check Permissions
                        if (!twitchBot.isPermitted(user, channel)) return false;

                        // Perform Action
                        switch (action) {
                            case "block": {
                                if (commandHandler.blacklistAdd(channel, command)) return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' wurde zur Blacklist hinzugefügt. YEPP");
                                else return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' ist bereits auf der Blacklist oder existiert nicht. YEPP");
                            }
                            case "unblock": {
                                if (commandHandler.blacklistRemove(channel, command)) return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' wurde von der Blacklist entfernt. YEPP");
                                else return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' war nicht auf der Blacklist oder existiert nicht. YEPP");
                            }
                        }
                    }

                    // Parse Target Channel
                    String targetChannelName = args.get(2);
                    while (targetChannelName.startsWith("@")) targetChannelName = targetChannelName.substring(1);
                    TwitchUser targetChannel = userHandler.getTwitchUser(targetChannelName.toLowerCase());

                    // Validate Target Channel
                    if (targetChannel == null) return twitchBot.sendMessage(event, name, "Fehler: Kanal '" + targetChannelName + "' nicht gefunden. YEPP");

                    // Check Permissions
                    if (twitchBot.isPermitted(user, targetChannel)) return false;

                    // Perform Action
                    switch (action) {
                        case "block": {
                            if (commandHandler.blacklistAdd(targetChannel, command)) return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' wurde zur Blacklist von hinzugefügt. YEPP");
                            else return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' ist bereits auf der Blacklist oder existiert nicht. YEPP");
                        }
                        case "unblock": {
                            if (commandHandler.blacklistRemove(targetChannel, command)) return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' wurde von der Blacklist entfernt. YEPP");
                            else return twitchBot.sendMessage(event, name, "Der Befehl '" + command + "' war nicht auf der Blacklist oder existiert nicht. YEPP");
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