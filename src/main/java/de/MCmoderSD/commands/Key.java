package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;

import static de.MCmoderSD.utilities.Calculate.*;

public class Key {

    // Constructor
    public Key(CommandHandler commandHandler, TwitchChat chat) {

        // Description
        String description = "Zeigt dir wo du das Spiel am Günstigen bekommst. Verwende " + commandHandler.getPrefix() + "key <Spiel>.";

        // Register command
        commandHandler.registerCommand(new Command(description, "key", "buy", "instant", "instant-gaming") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                for (var i = 0; i < args.length; i++) args[i] = args[i].replace("?", "");
                String game = String.join("+", args);
                while (game.endsWith("+")) game = game.trim();
                String url = "https://www.instant-gaming.com/de/suche/?igr=moder?q=" + game;
                chat.sendMessage(getChannel(event), "Hier bekommst du das Spiel am Günstigsten: " + url);
            }
        });
    }
}
