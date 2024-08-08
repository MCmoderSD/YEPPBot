package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Command;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.other.OpenAI;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Prompt {

    // Attributes
    private final int maxTokens;
    private final double temperature;
    private final String instruction;

    // Constructor
    public Prompt(BotClient botClient, MessageHandler messageHandler, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "prompt <Frage>";

        // About
        String[] name = {"prompt", "gpt", "chatgpt", "ai", "question", "yeppbot", "yepppbot"}; // Command name and aliases
        String description = "Benutzt ChatGPT, um eine Antwort auf eine Frage zu generieren. " + syntax;

        // Set Attributes
        JsonNode config = openAI.getConfig();
        maxTokens = config.get("maxTokens").asInt();
        temperature = config.get("temperature").asDouble();
        instruction = config.get("instruction").asText();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Send Message
                botClient.respond(event, getCommand(), openAI.prompt(botClient.getBotName(), instruction, trimMessage(processArgs(args)), maxTokens, temperature));
            }
        });
    }
}