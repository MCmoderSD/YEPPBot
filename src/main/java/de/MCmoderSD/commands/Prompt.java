package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.other.OpenAi;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Prompt {

    // Attributes
    private final String instruction;
    private final double temperature;
    private final int maxTokens;
    private final double topP;
    private final double frequencyPenalty;
    private final double presencePenalty;

    // Constructor
    public Prompt(BotClient botClient, MessageHandler messageHandler, OpenAi openAi) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "prompt <Frage>";

        // About
        String[] name = {"prompt", "gpt", "chatgpt", "ai", "question", "yeppbot", "yepppbot"}; // Command name and aliases
        String description = "Benutzt ChatGPT, um eine Antwort auf eine Frage zu generieren. " + syntax;

        // Set Attributes
        JsonNode config = openAi.getConfig();
        instruction = config.get("instruction").asText();
        temperature = config.get("temperature").asDouble();
        maxTokens = config.get("maxTokens").asInt();
        topP = config.get("topP").asDouble();
        frequencyPenalty = config.get("frequencyPenalty").asDouble();
        presencePenalty = config.get("presencePenalty").asDouble();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Send Message
                botClient.respond(event, getCommand(), formatOpenAiResponse(openAi.prompt(botClient.getBotName(), instruction, trimMessage(processArgs(args)), temperature, maxTokens, topP, frequencyPenalty, presencePenalty), "YEPP"));
            }
        });
    }
}