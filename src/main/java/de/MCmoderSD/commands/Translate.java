package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import de.MCmoderSD.core.CommandHandler;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.json.JsonUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.MCmoderSD.utilities.Calculate.*;

public class Translate {

    // Attributes
    private final String botName;
    private final OpenAiService openAI;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    // Constructor
    public Translate(CommandHandler commandHandler, TwitchChat chat, String botName) {

        // Description
        String description = "Kann deine Sätze in jede erdenkliche Sprache übersetzen: " + commandHandler.getPrefix() + "translate <Sprache> <Frage>";

        this.botName = botName;

        // Load Config
        JsonUtility jsonUtility = new JsonUtility();
        JsonNode config = jsonUtility.load("/api/ChatGPT.json");
        openAI = new OpenAiService(config.get("apiKey").asText());
        model = config.get("model").asText();
        maxTokens = config.get("maxTokens").asInt();
        temperature = 0; // For more subtle responses

        // Register command
        commandHandler.registerCommand(new Command(description, "translator", "translate", "übersetzer", "übersetze") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String language = args[0];
                String text = String.join(" ", args).replace(language, "");
                while (text.startsWith(" ") || text.startsWith("\n")) text = text.substring(1);
                while (text.endsWith(" ") || text.endsWith("\n")) text = text.trim();
                chat.sendMessage(getChannel(event), translate(language, text));
            }
        });
    }

    private String translate(String language, String text) {
        while (language.startsWith(" ") || language.startsWith("\n")) language = language.substring(1);
        while (language.endsWith(" ") || language.endsWith("\n")) language = language.trim();
        language = "Please translate the following text into " + language + ":";
        while (text.startsWith(" ") || text.startsWith("\n")) text = text.substring(1);
        while (text.endsWith(" ") || text.endsWith("\n")) text = text.trim();

        ChatMessage instruction = new ChatMessage(ChatMessageRole.SYSTEM.value(), language);
        ChatMessage prompt = new ChatMessage(ChatMessageRole.USER.value(), text);


        List<ChatMessage> messages = new ArrayList<>();
        messages.add(instruction);
        messages.add(prompt);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(model)
                .messages(messages)
                .n(1)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .user(botName)
                .logitBias(new HashMap<>())
                .build();

        StringBuilder response = new StringBuilder();

        openAI.streamChatCompletion(chatCompletionRequest)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(chatCompletionChunk -> chatCompletionChunk.getChoices()
                        .forEach(completion -> {
                            if(completion.getMessage().getContent() != null) response.append(completion.getMessage().getContent());
                        }));

        String finalResponse = response.toString();
        while (finalResponse.endsWith(" ") || finalResponse.endsWith("\n")) finalResponse = finalResponse.trim();
        while (finalResponse.startsWith(" ") || finalResponse.startsWith("\n")) finalResponse = finalResponse.substring(1);
        return finalResponse;
    }
}