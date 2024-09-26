package de.MCmoderSD.utilities.openAI.modules;

import com.fasterxml.jackson.databind.JsonNode;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;

import de.MCmoderSD.utilities.openAI.enums.ChatModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@SuppressWarnings("unused")
public class Chat {

    // Constants
    public static final int CHAR_PER_TOKEN = 4;

    // Associations
    private final ChatModel model;
    private final OpenAiService service;

    // Attributes
    private final JsonNode config;

    // Attributes
    private final HashMap<Integer, ArrayList<ChatMessage>> conversations;

    public Chat(ChatModel model, JsonNode config, OpenAiService service) {

        // Set Associations
        this.model = model;
        this.config = config;
        this.service = service;

        // Init Attributes
        conversations = new HashMap<>();
    }

    // Static Methods
    public static ChatMessage getChatMessage(ChatCompletionResult result) {
        return result.getChoices().getFirst().getMessage();
    }

    public static String getContent(ChatCompletionChunk chunk) {
        if (chunk == null) return null;
        else return chunk.getChoices().getFirst().getMessage().getContent();
    }

    public static ChatMessage addMessage(String text, boolean system) {
        return new ChatMessage(system ? ChatMessageRole.SYSTEM.value() : ChatMessageRole.USER.value(), text);
    }

    public static ArrayList<ChatMessage> filterMessages(ArrayList<ChatMessage> messages, boolean system) {
        ArrayList<ChatMessage> filtered = new ArrayList<>();
        for (ChatMessage message : messages) {
            String content = message.getContent();
            if (content == null || content.isEmpty() || content.isBlank()) continue;
            if (message.getRole().equals(system ? ChatMessageRole.SYSTEM.value() : ChatMessageRole.USER.value()))
                filtered.add(message);
        }
        return filtered;
    }

    public static int calculateTokens(String text) {
        return Math.ceilDiv(text.length(), CHAR_PER_TOKEN);
    }

    public static int calculateConversationLimit(int tokenLimit, int maxTokens) {
        var conversationLimit = 0;
        var totalTokens = 0;
        while (totalTokens <= tokenLimit) {
            totalTokens += totalTokens + maxTokens * 2;
            if (totalTokens <= tokenLimit) conversationLimit++;
        }

        return conversationLimit;
    }

    public static int calculateTokenSpendingLimit(int conversationLimit, int maxTokens) {
        var tokensUsed = 0;
        for (var i = 0; i < conversationLimit; i++) tokensUsed += tokensUsed + maxTokens * 2;
        return tokensUsed;
    }

    public static int calculateTotalTokens(ArrayList<ChatMessage> messages) {

        // Variables
        var totalTokensUsed = 0;
        var totalTokens = 0;

        // Calculate tokens
        for (ChatMessage message : messages) {

            // Check content
            String content = message.getContent();
            if (content == null || content.isEmpty() || content.isBlank()) continue;

            // Calculate tokens
            totalTokens += calculateTokens(content);
            if (messages.indexOf(message) > 1 && message.getRole().equals(ChatMessageRole.USER.value())) {
                totalTokensUsed += totalTokensUsed + totalTokens;
                totalTokens = 0;
            }
        }

        // Return total tokens
        return totalTokensUsed + totalTokens;
    }

    // Create Chat
    private ChatMessage chatCompleationRequest(String user, ArrayList<ChatMessage> messages, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Request
        ChatCompletionRequest request = ChatCompletionRequest
                .builder()                              // Builder
                .model(model.getModel())                // Model
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
        return getChatMessage(result);
    }

    // Create Chat Stream
    private ConnectableFlowable<ChatCompletionChunk> chatCompleationRequestStream(String user, ArrayList<ChatMessage> messages, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Request
        ChatCompletionRequest request = ChatCompletionRequest
                .builder()                              // Builder
                .model(model.getModel())                // Model
                .user(user)                             // User name
                .messages(messages)                     // Chat history
                .temperature(temperature)               // Temperature
                .maxTokens(maxTokens)                   // Max tokens
                .topP(topP)                             // Top P
                .frequencyPenalty(frequencyPenalty)     // Frequency penalty
                .presencePenalty(presencePenalty)       // Presence penalty
                .n(1)                                // Amount of completions
                .stream(true)                           // Stream
                .build();                               // Build

        return service.streamChatCompletion(request).publish();
    }

    // Check Parameters
    public boolean disprove(String user, String instruction, String prompt, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Check Username
        if (user == null) throw new IllegalArgumentException("User name is null");
        if (user.isEmpty() || user.isBlank()) throw new IllegalArgumentException("User name is empty");

        // Check Instruction
        if (instruction == null) throw new IllegalArgumentException("Instruction is null");
        if (instruction.isEmpty() || instruction.isBlank()) throw new IllegalArgumentException("Instruction is empty");

        // Check Prompt
        if (prompt == null) throw new IllegalArgumentException("Prompt is null");
        if (prompt.isEmpty() || prompt.isBlank()) throw new IllegalArgumentException("Prompt is empty");

        // Check Variables
        if (!model.checkTemperature(temperature)) throw new IllegalArgumentException("Invalid temperature");
        if (!model.checkTokens(maxTokens)) throw new IllegalArgumentException("Invalid max tokens");
        if (!model.checkTopP((int) topP)) throw new IllegalArgumentException("Invalid top P");
        if (!model.checkFrequencyPenalty((int) frequencyPenalty))
            throw new IllegalArgumentException("Invalid frequency penalty");
        if (!model.checkPresencePenalty((int) presencePenalty))
            throw new IllegalArgumentException("Invalid presence penalty");

        // Disapprove parameters
        return false;
    }

    // Start Conversation
    private String startConversation(int id, String user, String instruction, String message, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Add Instruction
        conversations.put(id, new ArrayList<>(Collections.singleton(new ChatMessage(ChatMessageRole.SYSTEM.value(), instruction))));
        addMessage(id, message, false);

        // Get response
        ChatMessage response = chatCompleationRequest(user, conversations.get(id), temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
        addMessage(id, response);

        // Return response
        return response.getContent();
    }

    // Continue Conversation
    private String continueConversation(int id, String user, String message, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Add message
        addMessage(id, message, false);

        // Get response
        ChatMessage response = chatCompleationRequest(user, conversations.get(id), temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
        addMessage(id, response);

        // Return response
        return response.getContent();
    }

    // Start Conversation Stream
    private ConnectableFlowable<ChatCompletionChunk> startConversationStream(int id, String user, String instruction, String message, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Add Instruction
        conversations.put(id, new ArrayList<>(Collections.singleton(new ChatMessage(ChatMessageRole.SYSTEM.value(), instruction))));
        addMessage(id, message, false);

        // Get response
        ConnectableFlowable<ChatCompletionChunk> response = chatCompleationRequestStream(user, conversations.get(id), temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
        addMessage(id, response);

        // Get response
        return chatCompleationRequestStream(user, conversations.get(id), temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
    }

    // Continue Conversation Stream
    private ConnectableFlowable<ChatCompletionChunk> continueConversationStream(int id, String user, String message, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Add message
        addMessage(id, message, false);

        // Get response
        ConnectableFlowable<ChatCompletionChunk> response = chatCompleationRequestStream(user, conversations.get(id), temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
        addMessage(id, response);

        // Get response
        return chatCompleationRequestStream(user, conversations.get(id), temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
    }

    // Prompt
    public String prompt(String user, String instruction, String prompt, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Approve parameters
        if (disprove(user, instruction, prompt, temperature, maxTokens, topP, frequencyPenalty, presencePenalty))
            throw new IllegalArgumentException("Invalid parameters");

        // Add messages
        ArrayList<ChatMessage> messages = new ArrayList<>();
        messages.add(addMessage(instruction, true));
        messages.add(addMessage(prompt, false));

        // Get response
        ChatMessage response = chatCompleationRequest(user, messages, temperature, maxTokens, topP, frequencyPenalty, presencePenalty);

        // Return response
        return response.getContent();
    }

    // Prompt Stream
    public Flowable<ChatCompletionChunk> promptStream(String user, String instruction, String prompt, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Approve parameters
        if (disprove(user, instruction, prompt, temperature, maxTokens, topP, frequencyPenalty, presencePenalty))
            throw new IllegalArgumentException("Invalid parameters");

        // Add messages
        ArrayList<ChatMessage> messages = new ArrayList<>();
        messages.add(addMessage(instruction, true));
        messages.add(addMessage(prompt, false));

        // Get response
        return chatCompleationRequestStream(user, messages, temperature, maxTokens, topP, frequencyPenalty, presencePenalty).autoConnect();
    }

    // Conversetion
    public String converse(int id, int maxConversationCalls, int maxTokenSpendingLimit, String user, String instruction, String message, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Approve parameters
        if (disprove(user, instruction, message, temperature, maxTokens, topP, frequencyPenalty, presencePenalty))
            throw new IllegalArgumentException("Invalid parameters");

        // Variables
        ArrayList<ChatMessage> conversation;

        // Continue conversation
        if (hasConversation(id)) conversation = conversations.get(id);
        else
            return startConversation(id, user, instruction, message, temperature, maxTokens, topP, frequencyPenalty, presencePenalty);

        // Check Limit
        boolean tokenSpendingLimit = calculateTotalTokens(conversation) + calculateTokens(message) + maxTokens >= maxTokenSpendingLimit;
        boolean conversationLimit = filterMessages(conversation, false).size() >= maxConversationCalls;

        // Continue conversation
        if (!(tokenSpendingLimit || conversationLimit))
            return continueConversation(id, user, message, temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
        else conversations.remove(id);

        // Return message
        if (conversationLimit)
            return "The conversation has reached the call limit of " + maxConversationCalls + " calls";
        else return "The conversation has reached the token limit of " + maxTokenSpendingLimit + " tokens";
    }

    // Conversetion Stream
    public Flowable<ChatCompletionChunk> converseStream(int id, int maxConversationCalls, int maxTokenSpendingLimit, String user, String instruction, String message, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Approve parameters
        if (disprove(user, instruction, message, temperature, maxTokens, topP, frequencyPenalty, presencePenalty))
            throw new IllegalArgumentException("Invalid parameters");

        // Variables
        ArrayList<ChatMessage> conversation;

        // Continue conversation
        if (hasConversation(id)) conversation = conversations.get(id);
        else
            return startConversationStream(id, user, instruction, message, temperature, maxTokens, topP, frequencyPenalty, presencePenalty).autoConnect();

        // Check Limit
        boolean tokenSpendingLimit = calculateTotalTokens(conversation) + calculateTokens(message) + maxTokens >= maxTokenSpendingLimit;
        boolean conversationLimit = filterMessages(conversation, false).size() >= maxConversationCalls;

        // Continue conversation
        if (!(tokenSpendingLimit || conversationLimit))
            return continueConversationStream(id, user, message, temperature, maxTokens, topP, frequencyPenalty, presencePenalty).autoConnect();
        else conversations.remove(id);
        return null;
    }

    // Add Message
    private void addMessage(int id, ChatMessage message) {
        conversations.get(id).add(message);
    }

    private void addMessage(int id, String text, boolean system) {
        conversations.get(id).add(new ChatMessage(system ? ChatMessageRole.SYSTEM.value() : ChatMessageRole.USER.value(), text));
    }

    private void addMessage(int id, ConnectableFlowable<ChatCompletionChunk> text) {
        StringBuilder message = new StringBuilder();
        text.subscribe(chunk -> message.append(getContent(chunk)));
        addMessage(id, message.toString(), true);
    }

    // Getter
    public JsonNode getConfig() {
        return config;
    }

    public ChatModel getModel() {
        return model;
    }

    public OpenAiService getService() {
        return service;
    }

    public boolean hasConversation(int id) {
        return conversations.containsKey(id);
    }

    public int getConversationSize(int id) {
        return conversations.get(id).size();
    }

    public int getConversationTokens(int id) {
        return calculateTotalTokens(conversations.get(id));
    }

    // Setter
    public void clearConversations() {
        conversations.clear();
    }

    public void clearConversation(int id) {
        conversations.remove(id);
    }

    // Calculate
    public BigDecimal calculatePromptCost(String inputText, String outputText) {
        return model.calculateCost(calculateTokens(inputText), calculateTokens(outputText));
    }

    public BigDecimal calculatePromptCost(String text) {
        return model.calculateCost(calculateTokens(text));
    }

    public BigDecimal calculatePromptCost(int inputTokens, int outputTokens) {
        return model.calculateCost(inputTokens, outputTokens);
    }

    public BigDecimal calculatePromptCost(int tokens) {
        return model.calculateCost(tokens);
    }

    public BigDecimal calculateConverationCost(int id) {
        return model.calculateCost(getConversationTokens(id));
    }
}