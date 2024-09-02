package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.OpenAI.OpenAI;
import de.MCmoderSD.utilities.OpenAI.modules.Chat;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Conversation {

    // Constructor
    public Conversation(BotClient botClient, MessageHandler messageHandler, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "chat <message/reset>)";

        // About
        String[] name = {"conversation", "chat", "unterhalten", "unterhaltung", "converse"}; // Command name and aliases
        String description = "Benutzt ChatGPT, um eine Unterhaltung zu beginnen. " + syntax;

        // Get Chat Module and Config
        Chat chat = openAI.getChat();
        JsonNode config = chat.getConfig();

        // Get Parameters
        int maxConversatitionCalls = config.get("maxConversationCalls").asInt();
        int maxTokenSpendingLimit = config.get("maxTokenSpendingLimit").asInt();
        double temperature = config.get("temperature").asDouble();
        int maxTokens = config.get("maxTokens").asInt();
        double topP = config.get("topP").asDouble();
        double frequencyPenalty = config.get("frequencyPenalty").asDouble();
        double presencePenalty = config.get("presencePenalty").asDouble();
        String instruction = config.get("instruction").asText();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check for end of conversation
                if (!args.isEmpty() && Arrays.asList("stop", "end", "clear", "hs", "reset").contains(args.getFirst().toLowerCase())) {
                    chat.clearConversation(event.getUserId());
                    botClient.respond(event, getCommand(), "Die Unterhaltung wurde beendet YEPP");
                    return;
                }

                // Send Message
                botClient.respond(event, getCommand(), formatOpenAIResponse(chat.converse(event.getUserId(), maxConversatitionCalls, maxTokenSpendingLimit, botClient.getBotName(), instruction, trimMessage(processArgs(args)), temperature, maxTokens, topP, frequencyPenalty, presencePenalty), "YEPP"));
            }
        });
    }
}