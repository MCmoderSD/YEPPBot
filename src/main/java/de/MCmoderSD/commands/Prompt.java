package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.other.OpenAI;

import static de.MCmoderSD.utilities.other.Calculate.getChannel;

public class Prompt {

    // Attributes
    private final int maxTokens;
    private final double temperature;
    private final String instruction;

    // Constructor
    public Prompt(CommandHandler commandHandler, TwitchChat chat, OpenAI openAI, String botName) {

        // Description
        String description = "Benutzt ChatGPT, um eine Antwort auf eine Frage zu generieren. Verwendung: " + commandHandler.getPrefix() + "prompt <Frage>";

        // Set Attributes
        JsonNode config = openAI.getConfig();
        maxTokens = config.get("maxTokens").asInt();
        temperature = config.get("temperature").asDouble();
        instruction = config.get("instruction").asText();

        // Register command
        commandHandler.registerCommand(new Command(description, "prompt", "gpt", "chatgpt", "ai", "question", "yeppbot", "yepppbot") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String question = String.join(" ", args);
                chat.sendMessage(getChannel(event), openAI.prompt(botName, instruction, question, maxTokens, temperature));
            }
        });
    }
}