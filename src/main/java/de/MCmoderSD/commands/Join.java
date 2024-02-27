package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;

public class Join {

    // Attributes
    private boolean firstJoin;
    private String channel;

    // Constructor
    public Join(CommandHandler commandHandler, TwitchChat chat) {

        // Initialize attributes
        revert();

        // Register command
        commandHandler.registerCommand(new Command("join") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                if (!firstJoin) {
                    firstJoin = true;
                    channel = event.getChannel().getName();
                    timer();
                    return;
                }

                if (channel != null && channel.equals(event.getChannel().getName())) {
                    chat.sendMessage(event.getChannel().getName(), "!join");
                    revert();
                }
            }
        });
    }

    // Reset attributes
    private void revert() {
        firstJoin = false;
        channel = null;
    }

    // Timer
    private void timer() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                revert();
            } catch (InterruptedException e) {
                System.out.println("Error: " + e);
            }
        }).start();
    }
}
