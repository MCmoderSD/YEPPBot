package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.MessageHelper.tagUser;

public class Queue extends CommandBuilder {

    // Constructor
    public Queue(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        String syntax = "Syntax: " + prefix + "Queue <leave|next|list|dequeue|clear> [User]";

        // About
        String[] name = { "Queue", "Warteliste", "Warteschlange" };
        String description = "Verwaltet die Warteliste für den Kanal: " + syntax;


        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Variables
                var argsSize = args.size();
                TwitchUser user = event.getUser();
                TwitchUser channel = event.getChannel();
                ArrayList<TwitchUser> queue = queueManager.getQueue(channel);


                // Join Queue
                if (args.isEmpty() || (argsSize == 1 && Arrays.asList("join", "enqueue").contains(args.getFirst().toLowerCase()))) {

                    // Check if User is already in Queue
                    if (queue.contains(user)) return twitchBot.sendMessage(event, name, tagUser(user) + ", du bist bereits in der Warteliste auf Position: " + (queue.indexOf(user) + 1) + " YEPP");

                    // Enqueue User
                    queueManager.enqueueUser(event);
                    return twitchBot.sendMessage(event, name, tagUser(user) + ", du wurdest der Warteliste hinzugefügt! Deine Position ist: " + (queue.size() + 1) + " YEPP");
                }

                // Parse Action
                String action = args.getFirst().toLowerCase();

                // Leave Queue
                if (argsSize == 1 && Arrays.asList("leave", "quit").contains(action)) {
                    if (queueManager.dequeueUser(user, channel)) return twitchBot.sendMessage(event, name, tagUser(user) + ", du wurdest aus der Warteliste entfernt. YEPP");
                    return twitchBot.sendMessage(event, name, tagUser(user) + ", Bro du warst nie auf der Warteliste? YEPP");
                }

                // List Queue
                if (Arrays.asList("list", "show").contains(action)) {

                    // Check if Queue is Empty
                    if (queue.isEmpty()) return twitchBot.sendMessage(event, name, "Die Warteliste ist aktuell leer. YEPP");

                    // Build Queue Message
                    StringBuilder queueMessage = new StringBuilder("Aktuelle Warteliste: ");
                    for (var i = 0; i < queue.size(); i++) {
                        TwitchUser queuedUser = queue.get(i);
                        queueMessage.append(i + 1).append(". ").append(tagUser(queuedUser));
                        if (i < queue.size() - 1) queueMessage.append(", ");
                    }

                    // Send Queue Message
                    return twitchBot.sendMessage(event, name, queueMessage.toString());
                }

                // Next in Queue
                if (action.equals("next")) {

                    // Check if Queue is Empty
                    if (queue.isEmpty()) return twitchBot.sendMessage(event, name, "Die Warteliste ist leer. YEPP");

                    // Get Next User and Wait Time
                    TwitchUser nextUser = queue.getFirst();
                    Timestamp joinedAt = queueManager.getJoinedAt(nextUser, channel);
                    var waitTimeMillis = System.currentTimeMillis() - joinedAt.getTime();
                    String formattedDuration = formatDuration(waitTimeMillis);

                    // Send Next User Message
                    return twitchBot.sendMessage(event, name, "Nächster in der Warteliste: " + tagUser(nextUser) + ", wartet seit " + formattedDuration + ". YEPP");
                }

                // Check Permissions
                if (!twitchBot.isPermitted(user, channel)) return false;

                // Clear Queue
                if (action.equals("clear")) {
                    queueManager.clearQueue(channel);
                    return twitchBot.sendMessage(event, name, "Die Warteliste wurde geleert. YEPP");
                }

                // Dequeue next or specific User
                if (Arrays.asList("dequeue", "remove").contains(action)) {

                    // Parse Target User
                    TwitchUser targetUser;
                    if (argsSize >= 2) {
                        String targetUserName = args.get(1);
                        while (targetUserName.startsWith("@")) targetUserName = targetUserName.substring(1);
                        targetUser = userHandler.getTwitchUser(targetUserName.toLowerCase());
                        if (targetUser == null) return twitchBot.sendMessage(event, name, "Fehler: Benutzer @" + targetUserName + " nicht gefunden. YEPP");
                    } else targetUser = queue.getFirst();

                    // Dequeue Target User
                    if (queueManager.dequeueUser(targetUser, channel)) return twitchBot.sendMessage(event, name, tagUser(targetUser) + " wurde aus der Warteliste entfernt. YEPP");
                    return twitchBot.sendMessage(event, name, tagUser(targetUser) + " ist nicht in der Warteliste. YEPP");
                }

                // Send Syntax Message
                return twitchBot.sendMessage(event, name, syntax);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    private static String formatDuration(long millis) {

        // Calculate time components
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        // Remainders
        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        // Build formatted string
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        // Return trimmed string
        return sb.toString().trim();
    }
}