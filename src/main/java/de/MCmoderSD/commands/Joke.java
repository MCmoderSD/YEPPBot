package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

import static de.MCmoderSD.utilities.Calculate.*;

public class Joke {

    // Attributes
    private final ArrayList<String> englishJokes, germanJokes;

    // Constructor
    public Joke(CommandHandler commandHandler, TwitchChat chat) {

        // Read jokes
        englishJokes = readJokes("/assets/english.jokes");
        germanJokes = readJokes("/assets/german.jokes");

        // Register command
        commandHandler.registerCommand(new Command("joke", "witz") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Determine language
                boolean isEnglish = false;
                if (args.length > 0)
                    isEnglish = args[0].equalsIgnoreCase("en") || args[0].equalsIgnoreCase("english") || args[0].equalsIgnoreCase("eng") || args[0].equalsIgnoreCase("englisch");
                ArrayList<String> jokes = isEnglish ? englishJokes : germanJokes;
                chat.sendMessage(getChannel(event), jokes.get((int) (Math.random() * jokes.size())));
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