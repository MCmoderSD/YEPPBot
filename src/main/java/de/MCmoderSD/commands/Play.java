package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.util.ArrayList;

public class Play {

    // Attributes
    private boolean firstPlay;
    private boolean sentMessage;
    private String channel;

    // Constructor
    public Play(BotClient botClient, MessageHandler messageHandler) {

        // About
        String[] name = {"play"};
        String description = "Sendet den Befehl " + botClient.getPrefix() + "play in den Chat, um Events beizutreten";

        // Initialize attributes
        sentMessage = false;
        reset();

        // Register command
        messageHandler.addCommand(new Command(description, name) {
            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {
                if (!firstPlay && !sentMessage) {
                    firstPlay = true;
                    channel = event.getChannel();
                    startResetTimer(10);
                    return;
                }

                if (!sentMessage && channel != null && channel.equals(event.getChannel())) {

                    // Send message
                    botClient.respond(event, getCommand(), BotClient.prefix + "play");

                    // Reset attributes
                    sentMessage = true;
                    startResetTimer(90);
                }
            }
        });
    }

    // Reset attributes
    private void reset() {
        firstPlay = false;
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