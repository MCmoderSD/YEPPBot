package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.util.ArrayList;

public class Join {

    // Attributes
    private boolean firstJoin;
    private boolean sentMessage;
    private String channel;

    // Constructor
    public Join(BotClient botClient, MessageHandler messageHandler) {

        // About
        String[] name = {"join"};
        String description = "Sendet den Befehl " + BotClient.prefix + "join in den Chat, um Events beizutreten";

        // Initialize attributes
        sentMessage = false;
        reset();

        // Register command
        messageHandler.addCommand(new Command(description, name) {
            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {
                if (!firstJoin && !sentMessage) {
                    firstJoin = true;
                    channel = event.getChannel();
                    startResetTimer(10);
                    return;
                }

                if (!sentMessage && channel != null && channel.equals(event.getChannel())) {

                    // Send message
                    String response = BotClient.prefix + "join";
                    botClient.respond(event, getCommand(), response);

                    // Reset attributes
                    sentMessage = true;
                    startResetTimer(90);
                }
            }
        });
    }

    // Reset attributes
    private void reset() {
        firstJoin = false;
        channel = null;
    }

    // Timer
    private void startResetTimer(int seconds) {
        new Thread(() -> {
            try {
                Thread.sleep(seconds * 1000L);
                reset();
                if (sentMessage) sentMessage = false;
            } catch (InterruptedException e) {
                System.out.println("Error: " + e);
            }
        }).start();
    }
}