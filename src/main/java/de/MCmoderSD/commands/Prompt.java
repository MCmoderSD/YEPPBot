package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.other.OpenAi;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Prompt {

    // Constructor
    public Prompt(BotClient botClient, MessageHandler messageHandler, OpenAi openAi) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "prompt <Frage>";

        // About
        String[] name = {"prompt", "gpt", "chatgpt", "ai", "question", "yeppbot", "yepppbot"}; // Command name and aliases
        String description = "Benutzt ChatGPT, um eine Antwort auf eine Frage zu generieren. " + syntax;

        // Set Attributes
        JsonNode config = openAi.getConfig();
        String instruction = config.get("instruction").asText();
        double temperature = config.get("temperature").asDouble();
        int maxTokens = config.get("maxTokens").asInt();
        double topP = config.get("topP").asDouble();
        double frequencyPenalty = config.get("frequencyPenalty").asDouble();
        double presencePenalty = config.get("presencePenalty").asDouble();
        String voice = config.get("voice").asText();
        String format = config.get("format").asText();
        double speed = config.get("speed").asDouble();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Send Message
                String response = formatOpenAiResponse(openAi.prompt(botClient.getBotName(), instruction, trimMessage(processArgs(args)), temperature, maxTokens, topP, frequencyPenalty, presencePenalty), "YEPP");
                botClient.respond(event, getCommand(), response);
                if (event.getChannel().equals("mcmodersd")) botClient.sendAudio(event, openAi.tts(response, voice, format, speed));
            }
        });
    }
}