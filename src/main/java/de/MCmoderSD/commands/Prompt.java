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

public class Prompt {

    // Attributes
    private final String botName;
    private final OpenAiService openAI;
    private final String model;
    private final int maxTokens;
    private final double temperature;
    private final ChatMessage instruction;

    // Constructor
    public Prompt(CommandHandler commandHandler, TwitchChat chat, String botName) {

        // Description
        String description = "Benutzt ChatGPT, um eine Antwort auf eine Frage zu generieren. Verwendung: " + commandHandler.getPrefix() + "prompt <Frage>";

        this.botName = botName;

        // Load Config
        JsonUtility jsonUtility = new JsonUtility();
        JsonNode config = jsonUtility.load("/api/ChatGPT.json");
        openAI = new OpenAiService(config.get("apiKey").asText());
        model = config.get("model").asText();
        maxTokens = config.get("maxTokens").asInt();
        temperature = config.get("temperature").asDouble();
        instruction = new ChatMessage(ChatMessageRole.SYSTEM.value(), config.get("instruction").asText());

        // Register command
        commandHandler.registerCommand(new Command(description, "prompt", "gpt", "chatgpt", "ai", "question", "yeppbot", "yepppbot") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String question = String.join(" ", args);
                chat.sendMessage(getChannel(event), prompt(question));
            }
        });
    }

    private String prompt(String question) {
        while (question.endsWith(" ") || question.endsWith("\n")) question = question.trim();

        ChatMessage prompt = new ChatMessage(ChatMessageRole.USER.value(), question);

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
        return finalResponse.replaceAll("YEPP[.,!?\\s]*", "YEPP ");
    }
}