package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Play {

    // Attributes
    private boolean firstPlay;
    private boolean sentMessage;
    private String channel;

    // Constructor
    public Play(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"play"};
        String description = "Sendet den Befehl " + commandHandler.getPrefix() + "play in den Chat, um Events beizutreten";


        // Initialize attributes
        sentMessage = false;
        reset();

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                if (!firstPlay && !sentMessage) {
                    firstPlay = true;
                    channel = getChannel(event);
                    startResetTimer(10);
                    return;
                }

                if (!sentMessage && channel != null && channel.equals(getChannel(event))) {

                    // Send message
                    String response = commandHandler.getPrefix() + "play";
                    chat.sendMessage(channel, response);

                    // Log response
                    mySQL.logResponse(event, getCommand(), processArgs(args), response);

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