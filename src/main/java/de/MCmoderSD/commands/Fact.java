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

public class Fact {

    // Attributes
    private final String[][] germanFacts;

    // Constructor
    public Fact(CommandHandler commandHandler, TwitchChat chat) {

        // Read facts
        germanFacts = readFacts("/assets/german.facts");

        // Register command
        commandHandler.registerCommand(new Command("fact", "fakt") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Generate fact
                StringBuilder fact = new StringBuilder();
                for (String[] word : germanFacts)
                    fact.append(word[(int) (Math.random() * word.length)]).append(" "); // Random fact

                chat.sendMessage(getChannel(event), fact.toString().trim() + '.');
            }
        });
    }

    // Read facts
    @SuppressWarnings("SameParameterValue")
    private String[][] readFacts(String path) {
        ArrayList<String> types = new ArrayList<>();

        // Read file
        try {
            InputStream inputStream = getClass().getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) types.add(line);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Split facts
        int typeSize = types.size();
        String[][] facts = new String[typeSize][];

        for (int i = 0; i < typeSize; i++) {
            String[] split = types.get(i).split(";");
            facts[i] = split;
        }

        return facts;
    }
}