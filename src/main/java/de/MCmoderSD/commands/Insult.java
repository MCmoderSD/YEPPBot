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

public class Insult {

    // Attributes
    private final ArrayList<String> germanInsults;

    // Constructor
    public Insult(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"insult", "beleidige", "mobbe", "mobbing"};
        String description = "Beleidigt einen Nutzer. Syntax: " + commandHandler.getPrefix() + "insult <Nutzer>.";


        // Read insults
        germanInsults = readInsults("/assets/german.insults");

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Generate Insult
                String insult = germanInsults.get((int) (Math.random() * germanInsults.size())); // Random insult
                String target = args.length > 0 ? args[0] : getAuthor(event);
                if (target.startsWith("@")) target = target.substring(1);
                String message = insult.replace("%member%", '@' + target);

                // Send message
                String response = trimMessage(message);
                chat.sendMessage(getChannel(event), message);

                // Log response
                if (mySQL != null) mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }

    // Read insults
    @SuppressWarnings("SameParameterValue")
    private ArrayList<String> readInsults(String path) {
        ArrayList<String> insults = new ArrayList<>();
        try {
            InputStream inputStream = getClass().getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) insults.add(line);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return insults;
    }
}