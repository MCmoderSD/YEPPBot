package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.other.OpenAi;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Conversation {

    // Attributes
    private final int maxTokensPerConversation;
    private final double temperature;
    private final int maxTokens;
    private final double topP;
    private final double frequencyPenalty;
    private final double presencePenalty;
    private final String instruction;

    // Constructor
    public Conversation(BotClient botClient, MessageHandler messageHandler, OpenAi openAi) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "chat <Nachricht>";

        // About
        String[] name = {"conversation", "chat", "unterhalten", "unterhaltung", "converse"}; // Command name and aliases
        String description = "Benutzt ChatGPT, um eine Unterhaltung zu beginnen. " + syntax;

        // Set Attributes
        JsonNode config = openAi.getConfig();
        maxTokensPerConversation = config.get("maxTokensPerConversation").asInt();
        temperature = config.get("temperature").asDouble();
        maxTokens = config.get("maxTokens").asInt();
        topP = config.get("topP").asDouble();
        frequencyPenalty = config.get("frequencyPenalty").asDouble();
        presencePenalty = config.get("presencePenalty").asDouble();
        instruction = config.get("instruction").asText();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check for end of conversation
                if (!args.isEmpty() && Arrays.asList("stop", "end", "clear", "hs").contains(args.getFirst().toLowerCase())) {
                    openAi.clearConversation(event.getUserId());
                    botClient.respond(event, getCommand(), "Die Unterhaltung wurde beendet. YEPP");
                    return;
                }

                // Send Message
                botClient.respond(event, getCommand(), formatOpenAiResponse(openAi.converse(event.getUserId(), maxTokensPerConversation, botClient.getBotName(), instruction, trimMessage(processArgs(args)), temperature, maxTokens, topP, frequencyPenalty, presencePenalty), "YEPP"));
            }
        });
    }
}