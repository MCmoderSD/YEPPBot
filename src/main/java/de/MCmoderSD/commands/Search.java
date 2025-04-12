package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.openai.objects.ChatPrompt;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Format.*;

public class Search {

    // Constructor
    public Search(BotClient botClient, MessageHandler messageHandler, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "search <query>";

        // About
        String[] name = {"search", "suche", "suchen", "websearch", "websuche"}; // Command name and aliases
        String description = "Use ChatGPT to search the web for information. " + syntax; // Command description

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Variables
                Integer randomId = Math.toIntExact(Math.round(Math.random() * 1000000));
                String input = trimMessage(concatArgs(args));
                String response;

                // Web Search
                ChatPrompt search = openAI.search(
                        null,
                        event.getUser(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        randomId,
                        input,
                        null
                );

                // Summarize if too long
                if (search.getText().length() <= 500) response = search.getText();
                else response = openAI.prompt(
                        null,
                        event.getUser(),
                        80L ,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "This message is way to long, please summarize it in less then 500 characters.",
                        randomId,
                        "Please summarize in the original language",
                        null
                ).getText();

                // Clear Chat History
                openAI.clearChatHistory(randomId);

                // Send Message
                botClient.respond(event, getCommand(), formatOpenAIResponse(response, "YEPP"));
            }
        });
    }
}