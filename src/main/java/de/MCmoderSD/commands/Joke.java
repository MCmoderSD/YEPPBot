package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;
import de.MCmoderSD.utilities.database.MySQL;

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
        englishJokes = readJokes("/assets/english.jokes");
        germanJokes = readJokes("/assets/german.jokes");

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Determine language
                boolean isEnglish = false;
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

    // Read jokes
    private ArrayList<String> readJokes(String path) {
        ArrayList<String> jokes = new ArrayList<>();
        try {
            InputStream inputStream = getClass().getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) jokes.add(line);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return jokes;
    }
}