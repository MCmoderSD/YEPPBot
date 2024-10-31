package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import de.MCmoderSD.OpenAI.modules.Chat;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Format.*;

public class Prompt {

    // Constructor
    public Prompt(BotClient botClient, MessageHandler messageHandler, Chat chat) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "prompt <Frage>";

        // About
        String[] name = {"prompt", "gpt", "chatgpt", "ai", "question", "yeppbot", "yepppbot"}; // Command name and aliases
        String description = "Benutzt ChatGPT, um eine Antwort auf eine Frage zu generieren. " + syntax;

        // Get Chat Config
        JsonNode config = chat.getConfig();

        // Get Parameters
        String instruction = config.get("instruction").asText();
        double temperature = config.get("temperature").asDouble();
        int maxTokens = config.get("maxTokens").asInt();
        double topP = config.get("topP").asDouble();
        double frequencyPenalty = config.get("frequencyPenalty").asDouble();
        double presencePenalty = config.get("presencePenalty").asDouble();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Send Message
                String response = formatOpenAIResponse(chat.prompt(botClient.getBotName(), instruction, trimMessage(processArgs(args)), temperature, maxTokens, topP, frequencyPenalty, presencePenalty), "YEPP");

                // Filter Response for argument injection
                while (response.startsWith("!")) response = response.substring(1);

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }
}