package de.MCmoderSD.utilities.openAI;

import com.fasterxml.jackson.databind.JsonNode;
import com.theokanning.openai.service.OpenAiService;
import de.MCmoderSD.utilities.openAI.enums.ChatModel;
import de.MCmoderSD.utilities.openAI.enums.ImageModel;
import de.MCmoderSD.utilities.openAI.enums.TTSModel;
import de.MCmoderSD.utilities.openAI.enums.TranscriptionModel;
import de.MCmoderSD.utilities.openAI.modules.Chat;
import de.MCmoderSD.utilities.openAI.modules.Image;
import de.MCmoderSD.utilities.openAI.modules.Speech;
import de.MCmoderSD.utilities.openAI.modules.Transcription;

@SuppressWarnings({"unused", "SwitchStatementWithTooFewBranches"})
public class OpenAI {

    // Constants
    private final JsonNode config;

    // Attributes
    private final OpenAiService service;

    // Modules
    private final boolean chatActive;
    private final boolean imageActive;
    private final boolean speechActive;
    private final boolean transcriptionActive;

    // Associations
    private final Chat chat;
    private final Image image;
    private final Speech speech;
    private final Transcription transcription;

    // Enums
    private final ChatModel chatModel;
    private final ImageModel imageModel;
    private final TTSModel ttsModel;
    private final TranscriptionModel transcriptionModel;

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
        transcriptionActive = config.has("transcription");

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

        // Initialize Transcription Module
        if (transcriptionActive) {

            // Get Transcription Config
            JsonNode transcriptionConfig = config.get("transcription");
            String transcriptionModelName = transcriptionConfig.get("transcriptionModel").asText();

            // Check Transcription Model
            if (transcriptionModelName == null) throw new IllegalArgumentException("Transcription model is null");
            if (transcriptionModelName.isEmpty() || transcriptionModelName.isBlank())
                throw new IllegalArgumentException("Transcription model is empty");

            // Set Transcription Model
            transcriptionModel = switch (transcriptionModelName) {
                case "whisper-1" -> TranscriptionModel.WHISPER;
                default -> throw new IllegalArgumentException("Invalid transcription model");
            };

            // Initialize Transcription
            transcription = new Transcription(transcriptionModel, transcriptionConfig, service);
        } else {
            transcriptionModel = null;
            transcription = null;
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

    public Transcription getTranscription() {
        return transcription;
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

    public boolean isTranscriptionActive() {
        return transcriptionActive;
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

    public TranscriptionModel getTranscriptionModel() {
        return transcriptionModel;
    }
}