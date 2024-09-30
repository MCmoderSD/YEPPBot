package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Play {

    // Attributes
    private final HashSet<Integer> firstPlay;
    private final HashMap<Integer, Timestamp> cooldownList;

    // Constructor
    public Play(BotClient botClient, MessageHandler messageHandler) {

        // About
        String[] name = {"play"};
        String description = "Sendet den Befehl " + botClient.getPrefix() + "play in den Chat, um Events beizutreten";

        // Initialize attributes
        firstPlay = new HashSet<>();
        cooldownList = new HashMap<>();

        // Register command
        messageHandler.addCommand(new Command(description, name) {
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

                // Check if first Play is already in the list
                if (firstPlay.contains(channelId)) {

                    // Add to cooldown list
                    cooldownList.put(channelId, new Timestamp(System.currentTimeMillis()));

                    // Remove from first Play list
                    firstPlay.remove(channelId);

                    // Send message
                    botClient.respond(event, getCommand(), botClient.getPrefix() + "play");
                } else firstPlay.add(channelId);
            }
        });
    }
}