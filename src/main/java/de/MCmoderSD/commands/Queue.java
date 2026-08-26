package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.MessageHelper.tagUser;

public class Queue extends CommandBuilder {

    // Constructor
    public Queue(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        var syntax = "Syntax: " + prefix + "queue <join|leave|list|position|next|open|close|clear> [username]";

        // About
        var name = new String[]{ "Queue", "Warteliste", "Warteschlange" };
        var description = "Verwaltet die Warteliste für den Kanal: " + syntax;


        // Register command
        var registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Variables
                var user = event.getUser();
                var channel = event.getChannel();
                var queue = queueManager.getQueue(channel);

                if (args.isEmpty()) {
                    if (queue.isEmpty()) {
                        return twitchBot.sendMessage(event, name, "Die Warteliste ist leer. YEPP");
                    } else {
                        var status = queueStatus(queue);
                        return twitchBot.sendMessage(event, name, status);
                    }
                }

                // Validate argument
                var argument = args.getFirst().toLowerCase();

                // Join the queue, permitted users may add someone else
                if (Arrays.asList("join", "beitreten", "enqueue", "add", "hinzufügen").contains(argument)) {

                    // Resolve the target, without a second argument the sender adds himself
                    var self = args.size() < 2;
                    var target = user;
                    if (!self) {

                        // Check Permissions
                        if (!twitchBot.isPermitted(user, channel)) return twitchBot.sendMessage(event, name, tagUser(user) + " Du hast keine Berechtigung, andere der Warteliste hinzuzufügen. YEPP");

                        // Resolve the target user
                        target = resolveUser(args.get(1));
                        if (target == null) return twitchBot.sendMessage(event, name, tagUser(user) + " Der Benutzer " + args.get(1) + " wurde nicht gefunden. YEPP");
                    }

                    // Validate if queue is open
                    if (!queueManager.isOpen(channel)) return twitchBot.sendMessage(event, name, "Die Warteliste ist derzeit geschlossen. YEPP");

                    // Check if user can join, a permitted user adding someone else overrides the requirement
                    if (self) {
                        var requirement = queueManager.getRequirement(channel);
                        if (!canJoin(user, channel, requirement)) return twitchBot.sendMessage(event, name, tagUser(user) + " " + denial(requirement));
                    }

                    // Add user to queue
                    if (queueManager.enqueueUser(target, channel)) {
                        var placement = queueManager.getPosition(target, channel);
                        return twitchBot.sendMessage(event, name, self ? tagUser(user) + " Du wurdest erfolgreich der Warteliste hinzugefügt. Deine Position ist: #" + placement + ". YEPP" : tagUser(target) + " wurde der Warteliste hinzugefügt. Position: #" + placement + ". YEPP");
                    }

                    // User is already in the queue
                    var placement = queueManager.getPosition(target, channel);
                    if (placement > 0) return twitchBot.sendMessage(event, name, self ? tagUser(user) + " Du bist bereits in der Warteliste auf Position: #" + placement + ". YEPP" : tagUser(target) + " ist bereits in der Warteliste auf Position: #" + placement + ". YEPP");
                    if (!queueManager.isOpen(channel)) return twitchBot.sendMessage(event, name, "Die Warteliste ist derzeit geschlossen. YEPP");
                    return twitchBot.sendMessage(event, name, "Die Warteliste ist voll. YEPP");
                }

                // Leave the queue, permitted users may remove someone else
                if (Arrays.asList("leave", "verlassen", "dequeue", "entfernen", "remove", "kick").contains(argument)) {

                    // Resolve the target, without a second argument the sender removes himself
                    var self = args.size() < 2;
                    var target = user;
                    if (!self) {

                        // Check Permissions
                        if (!twitchBot.isPermitted(user, channel)) return twitchBot.sendMessage(event, name, tagUser(user) + " Du hast keine Berechtigung, andere aus der Warteliste zu entfernen. YEPP");

                        // Resolve the target user
                        target = resolveUser(args.get(1));
                        if (target == null) return twitchBot.sendMessage(event, name, tagUser(user) + " Der Benutzer " + args.get(1) + " wurde nicht gefunden. YEPP");
                    }

                    // Remove user from queue
                    if (queueManager.dequeueUser(target, channel)) {
                        return twitchBot.sendMessage(event, name, self ? tagUser(user) + " Du wurdest erfolgreich aus der Warteliste entfernt. YEPP" : tagUser(target) + " wurde aus der Warteliste entfernt. YEPP");
                    } else {
                        return twitchBot.sendMessage(event, name, self ? tagUser(user) + " Du bist nicht in der Warteliste. YEPP" : tagUser(target) + " ist nicht in der Warteliste. YEPP");
                    }
                }

                // Display the queue status
                if (Arrays.asList("status", "list", "anzeigen").contains(argument)) {

                    if (queue.isEmpty()) {
                        return twitchBot.sendMessage(event, name, "Die Warteliste ist leer. YEPP");
                    } else {
                        var status = queueStatus(queue);
                        return twitchBot.sendMessage(event, name, status);
                    }
                }

                // Display current queue position of the user
                if (Arrays.asList("position", "platz", "platzierung").contains(argument)) {
                    var position = queue.indexOf(user);
                    if (position == -1) {
                        return twitchBot.sendMessage(event, name,  tagUser(user) + " Du bist nicht in der Warteliste. YEPP");
                    } else {
                        return twitchBot.sendMessage(event, name, tagUser(user) + " Deine aktuelle Position in der Warteliste ist: #" + (position + 1) + ". YEPP");
                    }
                }

                // Display the next user in the queue
                if (Arrays.asList("next", "nächster").contains(argument)) {
                    if (queue.isEmpty()) {
                        return twitchBot.sendMessage(event, name, "Die Warteliste ist leer. YEPP");
                    } else {
                        return twitchBot.sendMessage(event, name, "Der nächste in der Warteliste ist: " + tagUser(queue.getFirst()) + ". YEPP");
                    }
                }

                // Check Permissions
                if (!twitchBot.isPermitted(user, channel)) return twitchBot.sendMessage(event, name, tagUser(user) + " Du hast keine Berechtigung, diesen Befehl auszuführen. YEPP");

                // Open the queue
                if (Arrays.asList("open", "öffnen", "start").contains(argument)) {
                    var success = queueManager.setOpen(channel, true);
                    if (success) {
                        return twitchBot.sendMessage(event, name, "Die Warteliste wurde erfolgreich geöffnet. YEPP");
                    } else {
                        return twitchBot.sendMessage(event, name, "Die Warteliste ist bereits geöffnet. YEPP");
                    }
                }

                // Close the queue
                if (Arrays.asList("close", "schließen", "stop").contains(argument)) {
                    var success = queueManager.setOpen(channel, false);
                    if (success) {
                        return twitchBot.sendMessage(event, name, "Die Warteliste wurde erfolgreich geschlossen. YEPP");
                    } else {
                        return twitchBot.sendMessage(event, name, "Die Warteliste ist bereits geschlossen. YEPP");
                    }
                }

                // Clear the queue
                if (Arrays.asList("clear", "löschen", "reset").contains(argument)) {
                    queueManager.clearQueue(channel);
                    return twitchBot.sendMessage(event, name, "Die Warteliste wurde erfolgreich geleert. YEPP");
                }

                // Invalid command
                return twitchBot.sendMessage(event, name, "Unbekannter Befehl. " + syntax);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    // Resolve a TwitchUser from a command argument
    private TwitchUser resolveUser(String argument) {

        // Strip the leading tags
        var userName = argument;
        while (userName.startsWith("@")) userName = userName.substring(1);

        // Look up the user
        if (userName.isBlank()) return null;
        return userHandler.getTwitchUser(userName.toLowerCase());
    }

    // Check if a user can join the queue based on the requirement
    private boolean canJoin(TwitchUser user, TwitchUser channel, Requirement requirement) {

        // Broadcaster and Moderators always get through
        if (twitchBot.isPermitted(user, channel)) return true;

        // Check the Requirement
        return switch (requirement) {
            case EVERYONE -> true;
            case FOLLOWER -> twitchBot.isFollower(user, channel);
            case SUBSCRIBER -> twitchBot.isSubscriber(user, channel);
            case VIP -> twitchBot.isVIP(user, channel);
        };
    }

    // Build the Denial Message for a Requirement
    private static String denial(Requirement requirement) {
        return switch (requirement) {
            case EVERYONE -> "du kannst der Warteliste gerade nicht beitreten. YEPP";
            case FOLLOWER -> "nur Follower können der Warteliste beitreten. YEPP";
            case SUBSCRIBER -> "nur Subscriber können der Warteliste beitreten. YEPP";
            case VIP -> "nur VIPs können der Warteliste beitreten. YEPP";
        };
    }

    private static String queueStatus(ArrayList<TwitchUser> queue) {
        var status = new StringBuilder("Aktuelle Warteliste: ");

        // Build the queue status message
        for (var i = 0; i < queue.size(); i++) {
            var part = new StringBuilder();
            var user = queue.get(i);
            part.append("#").append(i + 1).append(" ").append(tagUser(user));
            if (i < queue.size() - 1) part.append(", ");

            // Check Char Limit (500) and truncate if necessary
            if (status.length() + part.length() > 475) {
                status.append("... und ").append(queue.size() - i).append(" weitere.");
                break;
            } else status.append(part);
        }

        return status.toString();
    }


    // Requirement Enum
    public enum Requirement implements Serializable {

        // Requirements
        EVERYONE, FOLLOWER, SUBSCRIBER, VIP;

        public static Requirement fromString(String value) {
            return switch (value.toLowerCase()) {
                case "follower" -> FOLLOWER;
                case "subscriber" -> SUBSCRIBER;
                case "vip" -> VIP;
                default -> EVERYONE;
            };
        }
    }
}