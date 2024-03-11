package de.MCmoderSD.utilities.other;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import de.MCmoderSD.utilities.json.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class OpenAI {

    // Attributes
    private final JsonNode config;
    private final String model;
    private final List<ChatMessage> messages;
    private final OpenAiService service;

    // Constructor
    public OpenAI(JsonNode config) {

        // Set Attributes
        this.config = config;
        model = config.get("model").asText();
        service = new OpenAiService(config.get("apiKey").asText());
        messages = new ArrayList<>();
    }

    private String contentGrabber(ChatCompletionChoice completion) {
        String content = completion.getMessage().getContent();
        return content == null ? "" : content;
    }

    // Setter
    public void clearMessages() {
        messages.clear();
    }

    public void addMessage(String text, boolean system) {
        messages.add(new ChatMessage(system ? ChatMessageRole.SYSTEM.value() : ChatMessageRole.USER.value(), trimMessage(text)));
    }

    // Getter
    public String prompt(String botName, String instruction, String prompt, int maxTokens, double temperature) {

        // Approve prompt
        if (botName == null) throw new IllegalArgumentException("Bot name is null");
        if (instruction == null) throw new IllegalArgumentException("Instruction is null");
        if (prompt == null) throw new IllegalArgumentException("Prompt is null");
        if (maxTokens < 1) throw new IllegalArgumentException("Max tokens is less than 1");
        if (temperature < 0) throw new IllegalArgumentException("Temperature is less than 0");
        if (temperature > 2) throw new IllegalArgumentException("Temperature is greater than 1");

        // Clear messages
        clearMessages();

        // Add messages
        addMessage(instruction, true);
        addMessage(prompt, false);

        // Request
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

        // Response
        StringBuilder response = new StringBuilder();
        service.streamChatCompletion(chatCompletionRequest).doOnError(Throwable::printStackTrace).blockingForEach(chatCompletionChunk -> chatCompletionChunk.getChoices().forEach(completion -> response.append(contentGrabber(completion))));

        // Return result
        String result = trimMessage(response.toString());
        return result.replaceAll("(?i)YEPP[.,!?\\s]*", "YEPP ");
    }

    public JsonNode getConfig() {
        return config;
    }
}
