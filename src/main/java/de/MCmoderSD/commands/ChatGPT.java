package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.openai.services.ChatService;

import java.util.ArrayList;
import java.util.Arrays;

import static com.openai.models.ReasoningEffort.*;
import static de.MCmoderSD.openai.models.ChatModel.*;
import static de.MCmoderSD.utilities.MessageHelper.*;

public class ChatGPT extends CommandBuilder {

    // Constructor
    public ChatGPT(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        var syntax = "Syntax: " + prefix + "ChatGPT <Message|reset>";

        // About
        var name = new String[]{ "ChatGPT", "Conversation", "GPT", "AI" };
        var description = "Interagiere mit ChatGPT! Sende eine Nachricht, um eine Antwort zu erhalten, oder verwende 'reset', um die Konversation zurückzusetzen. " + syntax;

        // Check if OpenAI is configured
        if (openAI == null) return;

        // Initialize ChatService
        var service = ChatService.builder()
                .setModel(GPT_5_4_NANO)
                .setReasoningEffort(NONE)
                .setMaxOutputTokens(80)
                .setInstructions(
                        """
                        You are a TwitchBot called YEPPBot.
                        Express yourself like a typical Twitch user, consistently using the YEPP in your sentences, especially at the end of each message.
                        Do not use standard emojis, only use common Twitch emotes, with a preference for the YEPP.
                        Keep your response short, less then 500 characters.
                        """
                )
                .build(openAI);

        // Register command
        var registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Check if message is empty
                if (args.isEmpty()) return twitchBot.sendMessage(event, name, "Bitte gib eine Nachricht ein. " + syntax);

                // Variables
                var user = event.getUser();

                // Check if user wants to reset the conversation
                if (args.size() == 1 && Arrays.asList("reset", "clear", "wipe", "new").contains(args.getFirst().toLowerCase())) {

                    // Reset conversation
                    openAIManger.deleteConversation(user);
                    messageHandler.updateConversation(user, null);

                    // Send confirmation message
                    return twitchBot.sendMessage(event, name, "Konversation zurückgesetzt! YEPP");
                }

                // Parse user message
                var userMessage = String.join(SPACE, args);
                var conversation = messageHandler.getConversation(user);

                // Create or continue conversation
                var prompt = conversation == null ? service.create(userMessage) : service.create(userMessage, conversation);

                // Save conversation
                messageHandler.updateConversation(user, prompt);

                // Send response
                return twitchBot.sendMessage(event, name, tagUser(user) + SPACE + formatOpenAI(prompt.getContent()));
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }
}