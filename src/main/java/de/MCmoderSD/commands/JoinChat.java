package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;

import java.util.Arrays;

import static de.MCmoderSD.utilities.Calculate.*;

public class JoinChat {

    // Constructor
    public JoinChat(CommandHandler commandHandler, TwitchChat chat, String[] admins) {

        // Register command
        commandHandler.registerCommand(new Command("joinchat", "addchat", "addtochat") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String channel = getChannel(event);

                if (tagAuthor(event).equals(channel))
                    join(event, chat, channel); // Broadcaster
                else if (Arrays.stream(admins).toList().contains(getAuthor(event).toLowerCase()))
                    join(event, chat, args[0]); // Admin
            }
        });
    }

    // Join chat
    private void join(ChannelMessageEvent event, TwitchChat chat, String... args) {
        chat.sendMessage(getChannel(event), "Joining " + args[0]);
        chat.joinChannel(args[0]);
    }
}