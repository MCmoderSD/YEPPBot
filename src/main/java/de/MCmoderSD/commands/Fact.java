package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.other.Reader;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Fact {

    // Attributes
    private final String[][] germanFacts;
    private final String[][] englishFacts;

    // Constructor
    public Fact(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // Syntax
        String syntax = "Syntax: " + commandHandler.getPrefix() + "fact en/de";

        // About
        String[] name = {"fact", "fakt"};
        String description = "Sendet einen zufälligen Fakt. " + syntax;


        // Read facts
        Reader reader = new Reader();
        englishFacts = readFacts(reader, "/assets/english.facts");
        germanFacts = readFacts(reader, "/assets/german.facts");

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Determine language
                boolean isEnglish = false;
                if (args.length > 0) isEnglish = args[0].toLowerCase().startsWith("en");
                String[][] facts = isEnglish ? englishFacts : germanFacts;

                // Generate fact
                StringBuilder fact = new StringBuilder();
                for (String[] strings : facts) fact.append(strings[(int) (Math.random() * strings.length)]).append(" "); // Random fact

                // Send message
                String response = trimMessage(fact.toString());
                chat.sendMessage(getChannel(event), response);

                // Log response
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }

    // Read facts
    private String[][] readFacts(Reader reader, String path) {
        ArrayList<String> lines = reader.lineRead(path);

        String[][] facts = new String[lines.size()][];
        for (var i = 0; i < lines.size(); i++) facts[i] = lines.get(i).split(";");
        return facts;
    }
}