package de.MCmoderSD.utilities.other;

import com.fasterxml.jackson.databind.JsonNode;

import com.theokanning.openai.audio.CreateSpeechRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import de.MCmoderSD.objects.AudioFile;
import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;
import okhttp3.ResponseBody;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class OpenAi {

    // Constants
    public static final int CHAR_PER_TOKEN = 4;

    // Config
    private final JsonNode config;

    // Attributes
    private final HashMap<Integer, ArrayList<ChatMessage>> conversations;
    private final OpenAiService service;

    // Enums
    private ChatModel chatModel;
    private TTSModel ttsModel;

    // Constructor
    public OpenAi(JsonNode config) {

        // Set Config
        this.config = config;
        String chat = config.get("chatModel").asText();
        String tts = config.get("ttsModel").asText();

        // Initialize Enums
        new Thread(() -> {

            // Wait for active
            try {
                while (!isActive()) //noinspection BusyWait
                    Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Error initializing OpenAI: " + e.getMessage());
            }

            // Set Chat Model
            if (chat == null) throw new IllegalArgumentException("Chat model is null");
            if (chat.isEmpty() || chat.isBlank()) throw new IllegalArgumentException("Chat model is empty");

            switch (chat) {
                case "gpt-4o":
                    chatModel = ChatModel.GPT_4O;
                    break;
                case "gpt-4o-2024-08-06":
                    chatModel = ChatModel.GPT_4O_2024_08_06;
                    break;
                case "gpt-4o-2024-05-13":
                    chatModel = ChatModel.GPT_4O_2024_05_13;
                    break;
                case "gpt-4o-mini":
                    chatModel = ChatModel.GPT_4O_MINI;
                    break;
                case "gpt-4o-mini-2024-07-18":
                    chatModel = ChatModel.GPT_4O_MINI_2024_07_18;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid chat model");
            }

            // Set TTS Model
            if (tts == null) throw new IllegalArgumentException("TTS model is null");
            if (tts.isEmpty() || tts.isBlank()) throw new IllegalArgumentException("TTS model is empty");

            switch (tts) {
                case "tts-1":
                    ttsModel = TTSModel.TTS;
                    break;
                case "tts-1-hd":
                    ttsModel = TTSModel.TTS_HD;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid TTS model");
            }
        }).start();

        // Initialize Attributes
        service = new OpenAiService(config.get("apiKey").asText());
        conversations = new HashMap<>();
    }

    // Static Methods
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

    public static int calculateTokens(String text) {
        return Math.ceilDiv(text.length(), CHAR_PER_TOKEN);
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

    public static ChatMessage addMessage(String text, boolean system) {
        return new ChatMessage(system ? ChatMessageRole.SYSTEM.value() : ChatMessageRole.USER.value(), text);
    }

    public static String getContent(ChatCompletionChunk chunk) {
        if (chunk == null) return null;
        else return chunk.getChoices().getFirst().getMessage().getContent();
    }

    public static ChatMessage getChatMessage(ChatCompletionResult result) {
        return result.getChoices().getFirst().getMessage();
    }

    // Methods
    private ChatMessage chatCompleationRequest(String user, ArrayList<ChatMessage> messages, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Request
        ChatCompletionRequest request = ChatCompletionRequest
                .builder()                              // Builder
                .model(chatModel.model)                 // Model
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

    private String continueConversation(int id, String user, String message, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Add message
        addMessage(id, message, false);

        // Get response
        ChatMessage response = chatCompleationRequest(user, conversations.get(id), temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
        addMessage(id, response);

        // Return response
        return response.getContent();
    }

    private ResponseBody createSpeech(String input, String voice, String format, double speed) {

        // Request
        CreateSpeechRequest request = CreateSpeechRequest
                .builder()                  // Builder
                .model(ttsModel.model)      // Model
                .input(input)               // Input
                .voice(voice)               // Voice
                .responseFormat(format)     // Format
                .speed(speed)               // Speed
                .build();                   // Build

        // Result
        return service.createSpeech(request);
    }

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
        if (!chatModel.checkTemperature(temperature)) throw new IllegalArgumentException("Invalid temperature");
        if (!chatModel.checkTokens(maxTokens)) throw new IllegalArgumentException("Invalid max tokens");
        if (!chatModel.checkTopP((int) topP)) throw new IllegalArgumentException("Invalid top P");
        if (!chatModel.checkFrequencyPenalty((int) frequencyPenalty))
            throw new IllegalArgumentException("Invalid frequency penalty");
        if (!chatModel.checkPresencePenalty((int) presencePenalty))
            throw new IllegalArgumentException("Invalid presence penalty");

        // Disapprove parameters
        return false;
    }

    public boolean disprove(String input, String voice, String format, double speed) {

        // Check Input
        if (input == null) throw new IllegalArgumentException("Input is null");
        if (!ttsModel.checkInput(input)) throw new IllegalArgumentException("Invalid input");

        // Check Voice
        if (voice == null) throw new IllegalArgumentException("Voice is null");
        if (!ttsModel.checkVoice(voice)) throw new IllegalArgumentException("Invalid voice");

        // Check Format
        if (format == null) throw new IllegalArgumentException("Format is null");
        if (!ttsModel.checkFormat(format)) throw new IllegalArgumentException("Invalid format");

        // Check Speed
        if (!ttsModel.checkSpeed(speed)) throw new IllegalArgumentException("Invalid speed");

        // Disapprove parameters
        return false;
    }

    // Stream
    private ConnectableFlowable<ChatCompletionChunk> chatCompleationRequestStream(String user, ArrayList<ChatMessage> messages, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {

        // Request
        ChatCompletionRequest request = ChatCompletionRequest
                .builder()                              // Builder
                .model(chatModel.model)                 // Model
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

    // TTS
    public AudioFile tts(String input, String voice, String format, double speed) throws UnsupportedAudioFileException, LineUnavailableException, IOException {

        // Approve parameters
        if (disprove(input, voice, format, speed))
            throw new IllegalArgumentException("Invalid parameters");

        // Get response
        ResponseBody response = createSpeech(input, voice, format, speed);
        if (response == null) throw new IllegalArgumentException("Invalid response");

        // Return audio
        return new AudioFile(response);
    }

    // Clear conversation
    public void clearConversations() {
        conversations.clear();
    }

    public void clearConversation(int id) {
        conversations.remove(id);
    }

    // Add message
    public void addMessage(int id, ChatMessage message) {
        conversations.get(id).add(message);
    }

    public void addMessage(int id, String text, boolean system) {
        conversations.get(id).add(new ChatMessage(system ? ChatMessageRole.SYSTEM.value() : ChatMessageRole.USER.value(), text));
    }

    public void addMessage(int id, ConnectableFlowable<ChatCompletionChunk> text) {
        StringBuilder message = new StringBuilder();
        text.subscribe(chunk -> message.append(getContent(chunk)));
        addMessage(id, message.toString(), true);
    }

    // Calculate
    public BigDecimal calculatePromptCost(String inputText, String outputText) {
        return chatModel.calculateCost(calculateTokens(inputText), calculateTokens(outputText));
    }

    public BigDecimal calculatePromptCost(String text) {
        return chatModel.calculateCost(calculateTokens(text));
    }

    public BigDecimal calculatePromptCost(int inputTokens, int outputTokens) {
        return chatModel.calculateCost(inputTokens, outputTokens);
    }

    public BigDecimal calculatePromptCost(int tokens) {
        return chatModel.calculateCost(tokens);
    }

    public BigDecimal calculateConverationCost(int id) {
        return chatModel.calculateCost(getConversationTokens(id));
    }

    public BigDecimal calculateTtsCost(String text) {
        return ttsModel.calculateCost(text.length());
    }

    public BigDecimal calculateTtsCost(int characters) {
        return ttsModel.calculateCost(characters);
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

    public boolean hasConversation(int id) {
        return conversations.containsKey(id);
    }

    // Models
    public enum ChatModel {

        // Models
        GPT_4O("gpt-4o", 0.00500, 0.01500),
        GPT_4O_2024_08_06("gpt-4o-2024-08-06", 0.0025, 0.01),
        GPT_4O_2024_05_13("gpt-4o-2024-05-13", 0.005, 0.015),
        GPT_4O_MINI("gpt-4o-mini", 0.00015, 0.0006),
        GPT_4O_MINI_2024_07_18("gpt-4o-mini-2024-07-18", 0.00015, 0.0006);

        // Attributes
        private final Set<String> models;
        private final double minTemperature;
        private final double maxTemperature;
        private final double minTopP;
        private final double maxTopP;
        private final double minFrequencyPenalty;
        private final double maxFrequencyPenalty;
        private final double minPresencePenalty;
        private final double maxPresencePenalty;
        private final String model;
        private final BigDecimal inputPrice;
        private final BigDecimal outputPrice;

        // Constructor
        ChatModel(String model, double input, double output) {

            // Initialize attributes
            models = new HashSet<>();
            minTemperature = 0.0;
            maxTemperature = 2.0;
            minTopP = 0;
            maxTopP = 1;
            minFrequencyPenalty = 0;
            maxFrequencyPenalty = 2;
            minPresencePenalty = 0;
            maxPresencePenalty = 2;

            // Set models
            models.add("gpt-4o");
            models.add("gpt-4o-2024-08-06");
            models.add("gpt-4o-2024-05-13");
            models.add("gpt-4o-mini");
            models.add("gpt-4o-mini-2024-07-18");

            // Set Attributes
            if (models.contains(model)) this.model = model;
            else throw new IllegalArgumentException("Invalid model");
            inputPrice = new BigDecimal(input).movePointLeft(4);
            outputPrice = new BigDecimal(output).movePointLeft(4);
        }

        // Methods
        public BigDecimal calculateCost(int inputTokens, int outputTokens) {
            return inputPrice.multiply(new BigDecimal(inputTokens)).add(outputPrice.multiply(new BigDecimal(outputTokens)));
        }

        public BigDecimal calculateCost(int tokens) {
            return inputPrice.multiply(new BigDecimal(tokens)).add(outputPrice.multiply(new BigDecimal(tokens)));
        }

        // Getter
        public Set<String> getModels() {
            return models;
        }

        public double getMinTemperature() {
            return minTemperature;
        }

        public double getMaxTemperature() {
            return maxTemperature;
        }

        public double getMinTopP() {
            return minTopP;
        }

        public double getMaxTopP() {
            return maxTopP;
        }

        public double getMinFrequencyPenalty() {
            return minFrequencyPenalty;
        }

        public double getMaxFrequencyPenalty() {
            return maxFrequencyPenalty;
        }

        public double getMinPresencePenalty() {
            return minPresencePenalty;
        }

        public double getMaxPresencePenalty() {
            return maxPresencePenalty;
        }

        public String getModel() {
            return model;
        }

        public BigDecimal getInputPrice() {
            return inputPrice;
        }

        public BigDecimal getOutputPrice() {
            return outputPrice;
        }

        // Check
        public boolean checkModel(String model) {
            return models.contains(model);
        }

        public boolean checkInput(String input) {
            return !input.isEmpty() || !input.isBlank();
        }

        public boolean checkTemperature(double temperature) {
            return temperature >= minTemperature && temperature <= maxTemperature;
        }

        public boolean checkTopP(int topP) {
            return topP >= minTopP && topP <= maxTopP;
        }

        public boolean checkFrequencyPenalty(double frequencyPenalty) {
            return frequencyPenalty >= minFrequencyPenalty && frequencyPenalty <= maxFrequencyPenalty;
        }

        public boolean checkPresencePenalty(double presencePenalty) {
            return presencePenalty >= minPresencePenalty && presencePenalty <= maxPresencePenalty;
        }

        public boolean checkTokens(int tokens) {
            if (model.equals("gpt-4o") || model.equals("gpt-4o-2024-08-06") || model.equals("gpt-4o-2024-05-13"))
                return tokens > 0 && tokens < 4096;
            if (model.equals("gpt-4o-mini") || model.equals("gpt-4o-mini-2024-07-18"))
                return tokens > 0 && tokens < 16384;
            return false;
        }
    }

    public enum TTSModel {

        // Models
        TTS("tts-1", 0.015),
        TTS_HD("tts-1-hd", 0.03);

        // Attributes
        private final Set<String> models;
        private final int maxCharacters;
        private final Set<String> voices;
        private final Set<String> formats;
        private final double minSpeed;
        private final double maxSpeed;
        private final Set<String> languages;
        private final String model;
        private final BigDecimal price;

        // Constructor
        TTSModel(String model, double price) {

            // Initialize attributes
            models = new HashSet<>();
            maxCharacters = 4096;
            voices = new HashSet<>();
            formats = new HashSet<>();
            minSpeed = 0.25;
            maxSpeed = 4.0;
            languages = new HashSet<>();

            // Set models
            models.add("tts-1");
            models.add("tts-1-hd");

            // Set voices
            voices.add("alloy");
            voices.add("echo");
            voices.add("fable");
            voices.add("onyx");
            voices.add("nova");
            voices.add("shimmer");

            // Set formats
            formats.add("mp3");
            formats.add("opus");
            formats.add("aac");
            formats.add("flac");
            formats.add("wav");
            formats.add("pcm");

            // Set languages
            languages.add("Afrikaans");
            languages.add("Arabic");
            languages.add("Armenian");
            languages.add("Azerbaijani");
            languages.add("Belarusian");
            languages.add("Bosnian");
            languages.add("Bulgarian");
            languages.add("Catalan");
            languages.add("Chinese");
            languages.add("Croatian");
            languages.add("Czech");
            languages.add("Danish");
            languages.add("Dutch");
            languages.add("English");
            languages.add("Estonian");
            languages.add("Finnish");
            languages.add("French");
            languages.add("Galician");
            languages.add("German");
            languages.add("Greek");
            languages.add("Hebrew");
            languages.add("Hindi");
            languages.add("Hungarian");
            languages.add("Icelandic");
            languages.add("Indonesian");
            languages.add("Italian");
            languages.add("Japanese");
            languages.add("Kannada");
            languages.add("Kazakh");
            languages.add("Korean");
            languages.add("Latvian");
            languages.add("Lithuanian");
            languages.add("Macedonian");
            languages.add("Malay");
            languages.add("Marathi");
            languages.add("Maori");
            languages.add("Nepali");
            languages.add("Norwegian");
            languages.add("Persian");
            languages.add("Polish");
            languages.add("Portuguese");
            languages.add("Romanian");
            languages.add("Russian");
            languages.add("Serbian");
            languages.add("Slovak");
            languages.add("Slovenian");
            languages.add("Spanish");
            languages.add("Swahili");
            languages.add("Swedish");
            languages.add("Tagalog");
            languages.add("Tamil");
            languages.add("Thai");
            languages.add("Turkish");
            languages.add("Ukrainian");
            languages.add("Urdu");
            languages.add("Vietnamese");
            languages.add("Welsh");

            // Set Attributes
            if (models.contains(model)) this.model = model;
            else throw new IllegalArgumentException("Invalid model");
            this.price = new BigDecimal(price).movePointLeft(4);
        }

        // Methods
        public BigDecimal calculateCost(int characters) {
            return price.multiply(new BigDecimal(characters));
        }

        // Getter
        public Set<String> getModels() {
            return models;
        }

        public int getMaxCharacters() {
            return maxCharacters;
        }

        public Set<String> getVoices() {
            return voices;
        }

        public Set<String> getFormats() {
            return formats;
        }

        public double getMinSpeed() {
            return minSpeed;
        }

        public double getMaxSpeed() {
            return maxSpeed;
        }

        public Set<String> getLanguages() {
            return languages;
        }

        public String getModel() {
            return model;
        }

        public BigDecimal getPrice() {
            return price;
        }

        // Check
        public boolean checkModel(String model) {
            return models.contains(model);
        }

        public boolean checkInput(String input) {
            return !input.isEmpty() && !input.isBlank() && input.length() <= maxCharacters;
        }

        public boolean checkVoice(String voice) {
            return voices.contains(voice);
        }

        public boolean checkFormat(String format) {
            return formats.contains(format);
        }

        public boolean checkSpeed(double speed) {
            return speed >= minSpeed && speed <= maxSpeed;
        }

        public boolean checkLanguage(String language) {
            return languages.contains(language);
        }
    }
}