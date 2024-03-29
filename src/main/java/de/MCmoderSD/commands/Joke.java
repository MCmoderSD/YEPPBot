package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.other.Reader;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Joke {

    // Attributes
    private final ArrayList<String> englishJokes, germanJokes;

    // Constructor
    public Joke(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // Syntax
        String syntax = "Syntax: " + commandHandler.getPrefix() + "joke en/de";

        // About
        String[] name = {"joke", "witz"};
        String description = "Sendet einen zufälligen Witz. " + syntax;


        // Read jokes
        Reader reader = new Reader();
        englishJokes = reader.lineRead("/assets/english.jokes");
        germanJokes = reader.lineRead("/assets/german.jokes");

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Determine language
                boolean isEnglish = false;
                if (args.length > 0) isEnglish = args[0].toLowerCase().startsWith("en");
                ArrayList<String> jokes = isEnglish ? englishJokes : germanJokes;

                // Send message
                String response = trimMessage(jokes.get((int) (Math.random() * jokes.size())));
                chat.sendMessage(getChannel(event), response);

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }
}