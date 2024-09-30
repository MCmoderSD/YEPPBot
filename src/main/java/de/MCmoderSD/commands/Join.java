package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Join {

    // Attributes
    private final HashSet<Integer> firstJoin;
    private final HashMap<Integer, Timestamp> cooldownList;

    // Constructor
    public Join(BotClient botClient, MessageHandler messageHandler) {

        // About
        String[] name = {"join"};
        String description = "Sendet den Befehl " + botClient.getPrefix() + "join in den Chat, um Events beizutreten";

        // Initialize attributes
        firstJoin = new HashSet<>();
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

                // Check if first join is already in the list
                if (firstJoin.contains(channelId)) {

                    // Add to cooldown list
                    cooldownList.put(channelId, new Timestamp(System.currentTimeMillis()));

                    // Remove from first join list
                    firstJoin.remove(channelId);

                    // Send message
                    botClient.respond(event, getCommand(), botClient.getPrefix() + "join");
                } else firstJoin.add(channelId);
            }
        });
    }
}