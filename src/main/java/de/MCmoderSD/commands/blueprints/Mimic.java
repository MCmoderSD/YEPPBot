package de.MCmoderSD.commands.blueprints;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public abstract class Mimic {

    // Attributes
    private final HashSet<Integer> firstMimic;
    private final HashMap<Integer, Timestamp> cooldownList;

    // Constructor
    public Mimic(BotClient botClient, MessageHandler messageHandler, String message) {

        // About
        String description = "Sendet den Befehl " + botClient.getPrefix() + message + " in den Chat, um Events beizutreten";

        // Initialize attributes
        firstMimic = new HashSet<>();
        cooldownList = new HashMap<>();

        // Register command
        messageHandler.addCommand(new Command(description, message) {
            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Variables
                var channelId = event.getChannelId();

                // Check cooldown
                if (cooldownList.containsKey(channelId)) {

                    // Get timestamp
                    var timestamp = cooldownList.get(channelId);
                    var currentTime = new Timestamp(System.currentTimeMillis());
                    var diff = currentTime.getTime() - timestamp.getTime();

                    // Check if 180 Seconds have passed
                    if (diff < 180000) return;
                }

                // Check if first Mimic is already in the list
                if (firstMimic.contains(channelId)) {

                    // Add to cooldown list
                    cooldownList.put(channelId, new Timestamp(System.currentTimeMillis()));

                    // Remove from first Mimic list
                    firstMimic.remove(channelId);

                    // Send message
                    botClient.respond(event, getCommand(), botClient.getPrefix() + message);
                } else firstMimic.add(channelId);
            }
        });
    }
}