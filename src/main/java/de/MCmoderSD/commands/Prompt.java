package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.other.OpenAI;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Prompt {

    // Attributes
    private final int maxTokens;
    private final double temperature;
    private final String instruction;

    // Constructor
    public Prompt(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, OpenAI openAI, String botName) {

        // Syntax
        String syntax = "Syntax: " + commandHandler.getPrefix() + "prompt <Frage>";

        // About
        String[] name = {"prompt", "gpt", "chatgpt", "ai", "question", "yeppbot", "yepppbot"}; // Command name and aliases
        String description = "Benutzt ChatGPT, um eine Antwort auf eine Frage zu generieren. " + syntax;

        // Set Attributes
        JsonNode config = openAI.getConfig();
        maxTokens = config.get("maxTokens").asInt();
        temperature = config.get("temperature").asDouble();
        instruction = config.get("instruction").asText();

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Send message and log
                String response = openAI.prompt(botName, instruction, trimMessage(processArgs(args)), maxTokens, temperature);
                chat.sendMessage(getChannel(event), response);
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }
}