package de.MCmoderSD.utilities.OpenAI;

import com.fasterxml.jackson.databind.JsonNode;
import com.theokanning.openai.service.OpenAiService;
import de.MCmoderSD.utilities.OpenAI.enums.ChatModel;
import de.MCmoderSD.utilities.OpenAI.enums.ImageModel;
import de.MCmoderSD.utilities.OpenAI.enums.TTSModel;
import de.MCmoderSD.utilities.OpenAI.modules.Chat;
import de.MCmoderSD.utilities.OpenAI.modules.Image;
import de.MCmoderSD.utilities.OpenAI.modules.Speech;

@SuppressWarnings("unused")
public class OpenAI {

    // Constants
    private final JsonNode config;

    // Attributes
    private final OpenAiService service;

    // Modules
    private final boolean chatActive;
    private final boolean imageActive;
    private final boolean speechActive;

    // Associations
    private final Chat chat;
    private final Image image;
    private final Speech speech;

    // Enums
    private final ChatModel chatModel;
    private final ImageModel imageModel;
    private final TTSModel ttsModel;

    // Constructor
    public OpenAI(JsonNode config) {

        // Set Config
        this.config = config;

        // Initialize OpenAI Service
        service = new OpenAiService(config.get("apiKey").asText());

        // Check Modules
        chatActive = config.has("chat");
        imageActive = config.has("image");
        speechActive = config.has("speech");

        // Initialize Chat Module
        if (chatActive) {

            // Get Chat Config
            JsonNode chatConfig = config.get("chat");
            String chatModelName = chatConfig.get("chatModel").asText();

            // Check Chat Model
            if (chatModelName == null) throw new IllegalArgumentException("Chat model is null");
            if (chatModelName.isEmpty() || chatModelName.isBlank())
                throw new IllegalArgumentException("Chat model is empty");

            // Set Chat Model
            chatModel = switch (chatModelName) {
                case "gpt-4o" -> ChatModel.GPT_4O;
                case "gpt-4o-2024-08-06" -> ChatModel.GPT_4O_2024_08_06;
                case "gpt-4o-2024-05-13" -> ChatModel.GPT_4O_2024_05_13;
                case "gpt-4o-mini" -> ChatModel.GPT_4O_MINI;
                case "gpt-4o-mini-2024-07-18" -> ChatModel.GPT_4O_MINI_2024_07_18;
                default -> throw new IllegalArgumentException("Invalid chat model");
            };

            // Initialize Chat
            chat = new Chat(chatModel, chatConfig, service);
        } else {
            chatModel = null;
            chat = null;
        }

        // Initialize Image Module
        if (imageActive) {

            // Get Image Config
            JsonNode imageConfig = config.get("image");
            String imageModelName = imageConfig.get("imageModel").asText();

            // Check Image Model
            if (imageModelName == null) throw new IllegalArgumentException("Image model is null");
            if (imageModelName.isEmpty() || imageModelName.isBlank())
                throw new IllegalArgumentException("Image model is empty");

            // Set Image Model
            imageModel = switch (imageModelName) {
                case "dall-e-2" -> ImageModel.DALL_E_2;
                case "dall-e-3" -> ImageModel.DALL_E_3;
                default -> throw new IllegalArgumentException("Invalid image model");
            };

            // Initialize Image
            image = new Image(imageModel, imageConfig, service);
        } else {
            imageModel = null;
            image = null;
        }

        // Initialize Speech Module
        if (speechActive) {

            // Get TTS Config
            JsonNode ttsConfig = config.get("speech");
            String ttsModelName = ttsConfig.get("ttsModel").asText();

            // Check TTS Model
            if (ttsModelName == null) throw new IllegalArgumentException("TTS model is null");
            if (ttsModelName.isEmpty() || ttsModelName.isBlank())
                throw new IllegalArgumentException("TTS model is empty");

            // Set TTS Model
            ttsModel = switch (ttsModelName) {
                case "tts-1" -> TTSModel.TTS;
                case "tts-1-hd" -> TTSModel.TTS_HD;
                default -> throw new IllegalArgumentException("Invalid TTS model");
            };

            // Initialize Speech
            speech = new Speech(ttsModel, ttsConfig, service);
        } else {
            ttsModel = null;
            speech = null;
        }
    }

    // Getter
    public JsonNode getConfig() {
        return config;
    }

    public boolean isActive() {
        return service != null;
    }

    public OpenAiService getService() {
        return service;
    }

    public Chat getChat() {
        return chat;
    }

    public Image getImage() {
        return image;
    }

    public Speech getSpeech() {
        return speech;
    }

    public boolean isChatActive() {
        return chatActive;
    }

    public boolean isImageActive() {
        return imageActive;
    }

    public boolean isSpeechActive() {
        return speechActive;
    }

    // Enums
    public ChatModel getChatModel() {
        return chatModel;
    }

    public ImageModel getImageModel() {
        return imageModel;
    }

    public TTSModel getTtsModel() {
        return ttsModel;
    }
}