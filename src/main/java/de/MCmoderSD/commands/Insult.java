package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Insult {

    // Constructor
    public Insult(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // Syntax
        String syntax = "Syntax: " + commandHandler.getPrefix() + "insult <Nutzer> <en/de>";

        // About
        String[] name = {"insult", "beleidige", "mobbe", "mobbing"};
        String description = "Beleidigt einen Nutzer. " + syntax;

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Determine language
                boolean isEnglish = false;
                if (args.length > 1) isEnglish = trimMessage(args[1]).toLowerCase().startsWith("en");
                String insult = mySQL.getInsult(isEnglish ? "en" : "de");

                // Gets target, insults the author if no target is provided
                String target = getAuthor(event);
                if (args.length > 0 && args[0].length() > 2) target = trimMessage(args[0]);
                if (target.startsWith("@")) target = target.substring(1);
                String message = insult.replace("%member%", '@' + target);

                // Send message
                String response = trimMessage(message);
                chat.sendMessage(getChannel(event), message);

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }
}