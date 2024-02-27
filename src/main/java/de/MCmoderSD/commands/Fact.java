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
                StringBuilder fact = new StringBuilder();
                for (String[] word : germanFacts)
                    fact.append(word[(int) (Math.random() * word.length)]).append(" "); // Random fact

                chat.sendMessage(event.getChannel().getName(), fact.toString().trim() + '.');
            }
        });
    }

    // Read facts
    @SuppressWarnings("SameParameterValue")
    private String[][] readFacts(String path) {
        ArrayList<String> types = new ArrayList<>();

        try {
            InputStream inputStream = getClass().getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8));
            
            String line;
            while ((line = reader.readLine()) != null) types.add(line);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        String[][] facts = new String[types.size()][];

        for (String line : types) {
            String[] split = line.split(";");
            facts[types.indexOf(line)] = split;
        }

        return facts;
    }
}
