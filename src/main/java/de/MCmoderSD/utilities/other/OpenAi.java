package de.MCmoderSD.utilities.other;

import com.fasterxml.jackson.databind.JsonNode;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@SuppressWarnings("unused")
public class OpenAi {

    // Constants
    private static final int CHAR_PER_TOKEN = 4;

    // Config
    private final JsonNode config;
    private final String chatModel;

    // Attributes
    private final HashMap<Integer, ArrayList<ChatMessage>> conversations;
    private final ArrayList<ChatMessage> messages;
    private final OpenAiService service;

    // Constructor
    public OpenAi(JsonNode config) {

        // Set Config
        this.config = config;
        chatModel = config.get("chatModel").asText();

        // Initialize Attributes
        service = new OpenAiService(config.get("apiKey").asText());
        conversations = new HashMap<>();
        messages = new ArrayList<>();
    }

    // Calculate

    public static int calculateConversationLimit(int tokenLimit, int maxTokens) {
        var conversationLimit = 0;
        var totalTokens = 0;
        while (totalTokens <= tokenLimit) {
            totalTokens += totalTokens + maxTokens * 2;
            conversationLimit++;
        }

        return conversationLimit;
    }

    public static int calculateTokenLimit(int conversationLimit, int maxTokens) {
        var tokenLimit = 0;
        for (var i = 0; i < conversationLimit; i++) tokenLimit += tokenLimit + maxTokens * 2;
        return tokenLimit;
    }

    public static boolean disprove(String user, String instruction, String prompt, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Approve parameters
        if (user == null) throw new IllegalArgumentException("User name is null");
        if (instruction == null) throw new IllegalArgumentException("Instruction is null");
        if (prompt == null) throw new IllegalArgumentException("Prompt is null");
        if (temperature < 0) throw new IllegalArgumentException("Temperature is less than 0");
        if (temperature > 2) throw new IllegalArgumentException("Temperature is greater than 2");
        if (maxTokens < 1) throw new IllegalArgumentException("Max tokens is less than 1");
        if (maxTokens > 16383) throw new IllegalArgumentException("Max tokens is greater than 16383");
        if (topP < 0) throw new IllegalArgumentException("Top P is less than 0");
        if (topP > 1) throw new IllegalArgumentException("Top P is greater than 1");
        if (frequencyPenalty < 0) throw new IllegalArgumentException("Frequency penalty is less than 0");
        if (frequencyPenalty > 2) throw new IllegalArgumentException("Frequency penalty is greater than 2");
        if (presencePenalty < 0) throw new IllegalArgumentException("Presence penalty is less than 0");
        if (presencePenalty > 2) throw new IllegalArgumentException("Presence penalty is greater than 2");

        // Disapprove parameters
        return false;
    }

    // Methods
    private ChatMessage chatCompleationRequest(String user, ArrayList<ChatMessage> messages, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Request
        ChatCompletionRequest request = ChatCompletionRequest
                .builder()                              // Builder
                .model(chatModel)                       // Model
                .user(user)                             // User name
                .messages(messages)                     // Chat history
                .temperature(temperature)               // Temperature
                .maxTokens(maxTokens)                   // Max tokens
                .topP(topP)                             // Top P
                .frequencyPenalty(frequencyPenalty)     // Frequency penalty
                .presencePenalty(presencePenalty)       // Presence penalty
                .n(1)                                // Amount of completions
                .stream(false)                          // Stream
                .build();                               // Build

        // Result
        ChatCompletionResult result = service.createChatCompletion(request);
        return result.getChoices().getFirst().getMessage();
    }

    private String startConversation(int id, String user, String instruction, String message, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Add Instruction
        conversations.put(id, new ArrayList<>(Collections.singleton(new ChatMessage(ChatMessageRole.SYSTEM.value(), instruction))));
        addMessage(id, message, false);

        // Get response
        ChatMessage response = chatCompleationRequest(user, conversations.get(id), temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
        addMessage(id, response.getContent(), true);

        // Return response
        return response.getContent();
    }

    private String continueConversation(int id, String user, String message, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Add message
        addMessage(id, message, false);

        // Get response
        ChatMessage response = chatCompleationRequest(user, conversations.get(id), temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
        addMessage(id, response.getContent(), true);

        // Return response
        return response.getContent();
    }

    private int calculateTotalTokens(ArrayList<ChatMessage> messages) {
        var totalTokens = 0;
        for (ChatMessage message : messages) totalTokens += message.getContent().length() / CHAR_PER_TOKEN;
        return totalTokens;
    }

    // Prompt
    public String prompt(String user, String instruction, String prompt, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Approve parameters
        if (disprove(user, instruction, prompt, temperature, maxTokens, topP, frequencyPenalty, presencePenalty))
            throw new IllegalArgumentException("Invalid parameters");

        // Clear messages
        clearMessages();

        // Add messages
        addMessage(instruction, true);
        addMessage(prompt, false);

        // Get response
        ChatMessage response = chatCompleationRequest(user, messages, temperature, maxTokens, topP, frequencyPenalty, presencePenalty);

        // Clear messages
        clearMessages();

        // Return response
        return response.getContent();
    }

    // Continue conversation
    public String converse(int id, int maxTokensPerConversation, String user, String instruction, String message, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Approve parameters
        if (disprove(user, instruction, message, temperature, maxTokens, topP, frequencyPenalty, presencePenalty))
            throw new IllegalArgumentException("Invalid parameters");

        // Variables
        ArrayList<ChatMessage> conversation;

        // Continue conversation
        if (conversations.containsKey(id)) conversation = conversations.get(id);
        else
            return startConversation(id, user, instruction, message, temperature, maxTokens, topP, frequencyPenalty, presencePenalty);

        // Check total tokens
        var totalTokens = calculateTotalTokens(conversation);
        if (totalTokens + 2 * maxTokens <= maxTokensPerConversation)
            return continueConversation(id, user, message, temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
        else conversations.remove(id);
        return "The conversation has reached the token limit";
    }

    // Setter
    public void clearMessages() {
        messages.clear();
    }

    public void clearConversation() {
        conversations.clear();
    }

    public void clearConversation(int id) {
        conversations.get(id).clear();
    }

    public void addMessage(String text, boolean system) {
        messages.add(new ChatMessage(system ? ChatMessageRole.SYSTEM.value() : ChatMessageRole.USER.value(), text));
    }

    public void addMessage(int id, String text, boolean system) {
        conversations.get(id).add(new ChatMessage(system ? ChatMessageRole.SYSTEM.value() : ChatMessageRole.USER.value(), text));
    }

    // Getter
    public JsonNode getConfig() {
        return config;
    }

    public boolean isActive() {
        return service != null;
    }

    public int getConversationSize(int id) {
        return conversations.get(id).size();
    }

    public int getConversationTokens(int id) {
        return calculateTotalTokens(conversations.get(id));
    }

    public enum ChatModel {

        // Models
        GPT_4O(0.00500, 0.01500),
        GPT_4O_2024_08_06(0.0025, 0.01),
        GPT_4O_2024_05_13(0.005, 0.015),
        GPT_4O_MINI(0.00015, 0.0006),
        GPT_4O_MINI_2024_07_18(0.00015, 0.0006);

        // Attributes
        private final BigDecimal inputPrice;
        private final BigDecimal outputPrice;

        // Constructor
        ChatModel(double input, double output) {
            this.inputPrice = new BigDecimal(input).movePointLeft(4);
            this.outputPrice = new BigDecimal(output).movePointLeft(4);
        }

        // Methods
        public BigDecimal calculateCost(int inputTokens, int outputTokens) {
            return inputPrice.multiply(new BigDecimal(inputTokens)).add(outputPrice.multiply(new BigDecimal(outputTokens)));
        }

        public BigDecimal calculateCost(int tokens) {
            return inputPrice.multiply(new BigDecimal(tokens)).add(outputPrice.multiply(new BigDecimal(tokens)));
        }

        // Getter
        public BigDecimal getInputPrice() {
            return inputPrice;
        }

        public BigDecimal getOutputPrice() {
            return outputPrice;
        }
    }
}