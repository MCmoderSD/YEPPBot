package de.MCmoderSD.utilities.OpenAI.modules;

import com.fasterxml.jackson.databind.JsonNode;
import com.theokanning.openai.audio.CreateSpeechRequest;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.ResponseBody;

import de.MCmoderSD.utilities.OpenAI.enums.TTSModel;
import de.MCmoderSD.objects.AudioFile;

import java.math.BigDecimal;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class Speech {

    // Associations
    private final TTSModel model;
    private final OpenAiService service;

    // Attributes
    private final JsonNode config;

    // Constructor
    public Speech(TTSModel model, JsonNode config, OpenAiService service) {

        // Set Associations
        this.model = model;
        this.config = config;
        this.service = service;
    }

    // Create Speech
    private ResponseBody createSpeech(String input, String voice, String format, double speed) {

        // Request
        CreateSpeechRequest request = CreateSpeechRequest
                .builder()                  // Builder
                .model(model.getModel())    // Model
                .input(input)               // Input
                .voice(voice)               // Voice
                .responseFormat(format)     // Format
                .speed(speed)               // Speed
                .build();                   // Build

        // Result
        return service.createSpeech(request);
    }

    // Check Parameters
    public boolean disprove(String input, String voice, String format, double speed) {

        // Check Input
        if (input == null) throw new IllegalArgumentException("Input is null");
        if (!model.checkInput(input)) throw new IllegalArgumentException("Invalid input");

        // Check Voice
        if (voice == null) throw new IllegalArgumentException("Voice is null");
        if (!model.checkVoice(voice)) throw new IllegalArgumentException("Invalid voice");

        // Check Format
        if (format == null) throw new IllegalArgumentException("Format is null");
        if (!model.checkFormat(format)) throw new IllegalArgumentException("Invalid format");

        // Check Speed
        if (!model.checkSpeed(speed)) throw new IllegalArgumentException("Invalid speed");

        // Disapprove parameters
        return false;
    }

    // Text to Speech
    public AudioFile tts(String input, String voice, String format, double speed) {

        // Approve parameters
        if (disprove(input, voice, format, speed))
            throw new IllegalArgumentException("Invalid parameters");

        // Get response
        ResponseBody response = createSpeech(input, voice, format, speed);
        if (response == null) throw new IllegalArgumentException("Invalid response");

        // Return audio
        return new AudioFile(response);
    }

    // Getter
    public JsonNode getConfig() {
        return config;
    }

    public TTSModel getModel() {
        return model;
    }

    public OpenAiService getService() {
        return service;
    }

    public BigDecimal calculatePrice(String input) {
        return model.calculateCost(input.length());
    }

    public BigDecimal calculatePrice(int characters) {
        return model.calculateCost(characters);
    }
}
