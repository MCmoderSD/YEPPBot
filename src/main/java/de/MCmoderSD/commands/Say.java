package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;

public class Say {

    // Constructor
    public Say(CommandHandler commandHandler, TwitchChat chat, String[] admins) {

        // Register command
        commandHandler.registerCommand(new Command("say", "repeat") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                for (String admin : admins)
                    if (event.getUser().getName().equals(admin.toLowerCase())) {
                        chat.sendMessage(event.getChannel().getName(), String.join(" ", args));
                        return;
                    }
            }
        });
    }
}
