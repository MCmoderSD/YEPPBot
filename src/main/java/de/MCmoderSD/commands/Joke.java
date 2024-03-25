package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.other.Reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Joke {

    // Attributes
    private final ArrayList<String> englishJokes, germanJokes;

    // Constructor
    public Joke(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"joke", "witz"};
        String description = "Sendet einen zufälligen Witz. Syntax: " + commandHandler.getPrefix() + "joke en/de";


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
                //todo: array of languages
                if (args.length > 0)
                    isEnglish = args[0].equalsIgnoreCase("en") || args[0].equalsIgnoreCase("english") || args[0].equalsIgnoreCase("eng") || args[0].equalsIgnoreCase("englisch");
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