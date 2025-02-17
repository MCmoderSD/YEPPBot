package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import de.MCmoderSD.OpenAI.modules.Chat;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Format.*;

public class Conversation {

    // Constants
    private final String conversationSuspended;

    // Constructor
    public Conversation(BotClient botClient, MessageHandler messageHandler, Chat chat) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "chat <message/reset>)";

        // About
        String[] name = {"conversation", "chat", "unterhalten", "unterhaltung", "converse"}; // Command name and aliases
        String description = "Benutzt ChatGPT, um eine Unterhaltung zu beginnen. " + syntax;

        // Constants
        conversationSuspended = "Die Unterhaltung wurde beendet YEPP";

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check for end of conversation
                if (!args.isEmpty() && Arrays.asList("stop", "end", "clear", "hs", "reset").contains(args.getFirst().toLowerCase())) {
                    chat.clearConversation(event.getUserId());
                    botClient.respond(event, getCommand(), conversationSuspended);
                    return;
                }

                // Send Message
                String response = formatOpenAIResponse(chat.converse(event.getUserId(), trimMessage(concatArgs(args))), "YEPP");

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }
}