package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;

import static de.MCmoderSD.utilities.Calculate.getAuthor;
import static de.MCmoderSD.utilities.Calculate.tagAuthor;

public class LeaveChat {
    // Constructor
    public LeaveChat(CommandHandler commandHandler, TwitchChat chat, String[] admins) {

        // Register command
        commandHandler.registerCommand(new Command("leavechat", "removechat", "removefromchat", "delfromchat") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                if (tagAuthor(event).equals(event.getChannel().getName()))
                    leave(event, chat, event.getChannel().getName());
                else for (String admin : admins)
                    if (getAuthor(event).equals(admin.toLowerCase())) leave(event, chat, args);
            }
        });
    }

    // Leave chat
    private void leave(ChannelMessageEvent event, TwitchChat chat, String... args) {
        chat.sendMessage(event.getChannel().getName(), "Leaving " + args[0]);
        chat.leaveChannel(args[0]);
    }
}