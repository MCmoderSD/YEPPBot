package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;

import static de.MCmoderSD.utilities.Calculate.*;

public class Join {

    // Attributes
    private boolean firstJoin;
    private boolean sentMessage;
    private String channel;

    // Constructor
    public Join(CommandHandler commandHandler, TwitchChat chat) {

        // Initialize attributes
        sentMessage = false;
        reset();

        // Register command
        commandHandler.registerCommand(new Command("join") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                if (!firstJoin) {
                    firstJoin = true;
                    channel = getChannel(event);
                    resetTimer(10);
                    return;
                }

                if (!sentMessage && channel != null && channel.equals(getChannel(event))) {
                    chat.sendMessage(channel, "!join");
                    sentMessage = true;
                    resetTimer(180);
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