package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Join {

    // Attributes
    private boolean firstJoin;
    private boolean sentMessage;
    private String channel;

    // Constructor
    public Join(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"join"};
        String description = "Sendet den Befehl " + commandHandler.getPrefix() + "join in den Chat, um Events beizutreten";


        // Initialize attributes
        sentMessage = false;
        reset();

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                if (!firstJoin) {
                    firstJoin = true;
                    channel = getChannel(event);
                    resetTimer(10);
                    return;
                }

                if (!sentMessage && channel != null && channel.equals(getChannel(event))) {

                    // Send message
                    String response = commandHandler.getPrefix() + "join";
                    chat.sendMessage(channel, response);

                    // Log response
                    if (mySQL != null) mySQL.logResponse(event, getCommand(), processArgs(args), response);

                    // Reset attributes
                    sentMessage = true;
                    resetTimer(120);
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
    private void resetTimer(int seconds) {
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