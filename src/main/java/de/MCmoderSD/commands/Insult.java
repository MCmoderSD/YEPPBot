package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.other.Reader;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Insult {

    // Attributes
    private final ArrayList<String> englishInsults;
    private final ArrayList<String> germanInsults;

    // Constructor
    public Insult(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"insult", "beleidige", "mobbe", "mobbing"};
        String description = "Beleidigt einen Nutzer. Syntax: " + commandHandler.getPrefix() + "insult <Nutzer>.";


        // Read insults
        Reader reader = new Reader();
        englishInsults = reader.lineRead("/assets/english.insults");
        germanInsults = reader.lineRead("/assets/german.insults");

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Determine language
                boolean isEnglish = false;
                if (args.length > 0) isEnglish = args[0].toLowerCase().startsWith("en");
                ArrayList<String> insults = isEnglish ? englishInsults : germanInsults;

                // Generate Insult
                String insult = insults.get((int) (Math.random() * insults.size())); // Random insult

                // Gets target, insults the author if no target is provided
                String target = args.length > 0 ? args[0] : getAuthor(event);
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