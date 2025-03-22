package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.openai.objects.ChatHistory;
import de.MCmoderSD.utilities.database.SQL;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Format.*;

public class Conversation {

    // Constructor
    public Conversation(BotClient botClient, MessageHandler messageHandler, SQL sql, OpenAI openAI, JsonNode config) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "chat <message/reset>)";

        // About
        String[] name = {"conversation", "chat", "unterhalten", "unterhaltung", "converse"}; // Command name and aliases
        String description = "Benutzt ChatGPT, um eine Unterhaltung zu beginnen. " + syntax;

        // Constants
        String conversationSuspended = "Die Unterhaltung wurde beendet YEPP";
        String tokenLimitExceeded = "Das Token-Limit wurde überschritten. Die Unterhaltung wurde beendet YEPP";
        JsonNode chat = config.get("chat");
        var priceFactor = (float) chat.get("priceFactor").asDouble();
        var tokenSpendingLimit = chat.get("spendingLimit").asLong();

        // ToDo: Complete when Serialization is working

        // Attributes
        //AssetManager assetManager = sql.getAssetManager();
        //assetManager.getChatHistory().forEach(openAI::addChatHistory);

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                var userId = event.getUserId();

                // Check for end of conversation
                if (!args.isEmpty() && Arrays.asList("stop", "end", "clear", "hs", "reset").contains(args.getFirst().toLowerCase())) {
                    openAI.clearChatHistory(userId);
                    botClient.respond(event, getCommand(), conversationSuspended);
                    return;
                }

                // Check Token Spending Limit
                if (openAI.chatHistoryExists(userId)) {

                    // Get Chat History and calculate effective spending
                    ChatHistory chatHistory = openAI.getChatHistory(userId);
                    var inputTokens = chatHistory.getInputTokens();
                    var outputTokens = chatHistory.getOutputTokens();
                    var effectiveSpending = inputTokens * priceFactor + outputTokens;
                    if (effectiveSpending > tokenSpendingLimit) {
                        openAI.clearChatHistory(userId);
                        //assetManager.setChatHistory(userId, chatHistory);
                        botClient.respond(event, getCommand(), tokenLimitExceeded);
                        return;
                    }
                }

                // Send Message
                String response = formatOpenAIResponse(openAI.prompt(event.getUser(), userId, trimMessage(concatArgs(args))), "YEPP");

                // Save Chat History
                //assetManager.setChatHistory(userId, openAI.getChatHistory(userId));

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }
}