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

public class Fact {

    // Attributes
    private final String[][] germanFacts;
    private final String[][] englishFacts;

    // Constructor
    public Fact(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"fact", "fakt"};
        String description = "Sendet einen zufälligen Fakt. Syntax: " + commandHandler.getPrefix() + "fact en/de";


        // Read facts
        englishFacts = readFacts("/assets/english.facts");
        germanFacts = readFacts("/assets/german.facts");

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Determine language
                boolean isEnglish = false;
                if (args.length > 0)
                    isEnglish = args[0].equalsIgnoreCase("en") || args[0].equalsIgnoreCase("english") || args[0].equalsIgnoreCase("eng") || args[0].equalsIgnoreCase("englisch");
                String[][] facts = isEnglish ? englishFacts : germanFacts;

                // Generate fact
                StringBuilder fact = new StringBuilder();
                for (String[] word : facts) fact.append(word[(int) (Math.random() * word.length)]).append(" "); // Random fact

                // Send message
                String response = trimMessage(fact.toString());
                chat.sendMessage(getChannel(event), response);

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }

    // Read facts
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
        var typeSize = types.size();
        String[][] facts = new String[typeSize][];

        for (var i = 0; i < typeSize; i++) {
            String[] split = types.get(i).split(";");
            facts[i] = split;
        }

        return facts;
    }
}