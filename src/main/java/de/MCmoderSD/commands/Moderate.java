package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.enums.Scope;
import de.MCmoderSD.helix.handler.*;
import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Stream;

import static de.MCmoderSD.utilities.MessageHelper.SPACE;

public class Moderate extends CommandBuilder {

    // Constructor
    public Moderate(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        String syntax = "Syntax: " + prefix + "moderate join/leave/block/unblock/authenticate command/channel";

        // About
        String[] name = {"moderate", "mod", "moderrate", "modderate", "modderrate"};
        String description = "Ändert die Einstellungen des Bots. " + syntax;

        Scope[] scopes = new ArrayList<>(Arrays.asList(UserHandler.REQUIRED_SCOPES, ChatHandler.REQUIRED_SCOPES, RoleHandler.REQUIRED_SCOPES, StreamHandler.REQUIRED_SCOPES, ChannelHandler.REQUIRED_SCOPES))
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
                var response = syntax;
                var noArgs = args.isEmpty();

                // Check args
                if (noArgs) return twitchBot.sendMessage(event, name, response);

                // Get Action
                String action = args.getFirst().toLowerCase();
                if (!Arrays.asList("join", "leave", "block", "unblock", "authenticate", "auth", "oauth").contains(action)) return twitchBot.sendMessage(event, name, response);

                // Authenticate Action
                if (Arrays.asList("authenticate", "auth", "oauth").contains(action)) return twitchBot.sendMessage(event, name, "Authenticate here: " + helixHandler.getAuthorizationUrl(scopes));

                // Join/Leave Action
                if (Arrays.asList("join", "leave").contains(action)) {

                    // Variables
                    boolean noTarget = args.size() == 1;
                    String target;

                    // Join Channel (self)
                    if (noTarget && action.equals("join")) {
                        if (twitchBot.joinChannel(user)) return twitchBot.sendMessage(event, name, "Join channel: " + user.getDisplayName());
                        else return twitchBot.sendMessage(event, name, "Fehler: Konnte dem Channel nicht beitreten: " + user.getDisplayName());
                    }

                    // Leave Channel (self)
                    if (noTarget && action.equals("leave")) {
                        if (twitchBot.leaveChannel(user)) return twitchBot.sendMessage(event, name, "Leave channel: " + user.getDisplayName());
                        else return twitchBot.sendMessage(event, name, "Fehler: Konnte den Channel nicht verlassen: " + user.getDisplayName());
                    }


                    // Get Target
                    target = args.get(1).toLowerCase().replace("@", "");
                    var targetUser = userHandler.getTwitchUser(target);
                    if (targetUser == null) return twitchBot.sendMessage(event, name, "Fehler: Benutzer '" + target + "' nicht gefunden.");


                    // Join Channel (target)
                    if (action.equals("join") && twitchBot.isOwner(user)) {
                        if (twitchBot.joinChannel(targetUser)) return twitchBot.sendMessage(event, name, "Join channel: " + targetUser.getDisplayName());
                        else return twitchBot.sendMessage(event, name, "Fehler: Konnte dem Channel nicht beitreten: " + targetUser.getDisplayName());
                    }

                    // Leave Channel (target)
                    if (action.equals("leave") && (twitchBot.isOwner(user)) || twitchBot.isPermitted(user, channel) && targetUser.getId().equals(channel.getId())) {
                        if (twitchBot.leaveChannel(targetUser)) return twitchBot.sendMessage(event, name, "Leave channel: " + targetUser.getDisplayName());
                        else return twitchBot.sendMessage(event, name, "Fehler: Konnte den Channel nicht verlassen: " + targetUser.getDisplayName());
                    }

                    // No Permission
                    return false;
                }

                // Block/Unblock Action
                if (Arrays.asList("block", "unblock").contains(action)) {

                    // Check Args
                    if (args.size() < 2) return twitchBot.sendMessage(event, name, response);

                    // Get Command/Channel to Block/Unblock
                    String command = args.get(1).toLowerCase();
                    boolean noTarget = args.size() == 2;
                    String target;

                    // Block Action
                    if (noTarget && action.equals("block") && twitchBot.isPermitted(user, channel)) {
                        if (commandHandler.blacklistAdd(channel, command)) response = "Der Befehl '" + command + "' wurde zur Blacklist hinzugefügt.";
                        else response = "Der Befehl '" + command + "' ist bereits auf der Blacklist oder existiert nicht.";
                        return twitchBot.sendMessage(event, name, response);
                    }

                    // Unblock Action
                    if (noTarget && action.equals("unblock") && twitchBot.isPermitted(user, channel)) {
                        if (commandHandler.blacklistRemove(channel, command)) response = "Der Befehl '" + command + "' wurde von der Blacklist entfernt.";
                        else response = "Der Befehl '" + command + "' war nicht auf der Blacklist oder existiert nicht.";
                        return twitchBot.sendMessage(event, name, response);
                    }

                    // No Target specified
                    if (noTarget) return twitchBot.sendMessage(event, name, response);

                    // Get Target
                    target = args.get(2).toLowerCase().replace("@", "");
                    var targetUser = userHandler.getTwitchUser(target);
                    if (targetUser == null) return twitchBot.sendMessage(event, name, "Fehler: Benutzer '" + target + "' nicht gefunden.");

                    // Block Action (target channel)
                    if (action.equals("block") && (targetUser.getId().equals(user.getId()) || twitchBot.isOwner(user))) {
                        if (commandHandler.blacklistAdd(targetUser, command)) response = "Der Befehl '" + command + "' wurde zur Blacklist von '" + targetUser.getDisplayName() + "' hinzugefügt.";
                        else response = "Der Befehl '" + command + "' ist bereits auf der Blacklist von '" + targetUser.getDisplayName() + "' oder existiert nicht.";
                        return twitchBot.sendMessage(event, name, response);
                    }

                    // Unblock Action (target channel)
                    if (action.equals("unblock") && (targetUser.getId().equals(user.getId()) || twitchBot.isOwner(user))) {
                        if (commandHandler.blacklistRemove(targetUser, command)) response = "Der Befehl '" + command + "' wurde von der Blacklist von '" + targetUser.getDisplayName() + "' entfernt.";
                        else response = "Der Befehl '" + command + "' war nicht auf der Blacklist von '" + targetUser.getDisplayName() + ". oder existiert nicht.";
                        return twitchBot.sendMessage(event, name, response);
                    }

                    // Send Response
                    return twitchBot.sendMessage(event, name, "Insufficient permissions to block/unblock commands for other channels.");
                }

                // Send Message
                return twitchBot.sendMessage(event, name, response);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }
}