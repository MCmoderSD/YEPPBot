package de.MCmoderSD.utilities.openAI.modules;

import com.fasterxml.jackson.databind.JsonNode;
import com.theokanning.openai.audio.CreateTranscriptionRequest;
import com.theokanning.openai.audio.TranscriptionResult;
import com.theokanning.openai.service.OpenAiService;
import de.MCmoderSD.utilities.openAI.enums.TranscriptionModel;
import de.MCmoderSD.objects.AudioFile;

import java.io.File;
import java.math.BigDecimal;

import static de.MCmoderSD.utilities.other.Calculate.getSHA256;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class Transcription {

    // Associations
    private final TranscriptionModel model;
    private final OpenAiService service;

    // Attributes
    private final JsonNode config;

    // Constructor
    public Transcription(TranscriptionModel model, JsonNode config, OpenAiService service) {

        // Set Associations
        this.model = model;
        this.config = config;
        this.service = service;
    }

    // Create Transcription
    private TranscriptionResult createTranscription(File audioFile, String prompt, String language, double temperature) {

        // Request
        CreateTranscriptionRequest request = CreateTranscriptionRequest
                .builder()                          // Builder
                .model(model.getModel())            // Model
                .prompt(prompt)                     // Prompt
                .language(language.toLowerCase())   // Language
                .temperature(temperature)           // Temperature
                .build();                           // Build

        // Result
        return service.createTranscription(request, audioFile);
    }

    // Check Parameters
    public boolean disprove(File file, String language, double temperature) {

        // Check File
        if (file == null) throw new IllegalArgumentException("Input is null");
        if (!model.checkInput(file)) throw new IllegalArgumentException("Invalid input");

        // Check Language
        if (language == null) throw new IllegalArgumentException("Language is null");
        if (!model.checkLanguage(language)) throw new IllegalArgumentException("Invalid language");

        // Check Temperature
        if (!model.checkTemperature(temperature)) throw new IllegalArgumentException("Invalid temperature");

        // Disapprove parameters
        return false;
    }

    // Transcribe
    public String transcribe(AudioFile audioFile, String prompt, String language, double temperature) {

        // Check Parameters
        String path = System.getProperty("java.io.tmpdir") + getSHA256(audioFile.getAudioData()) + ".wav";

        // Export Audio File
        File file = audioFile.exportToWav(path);

        // Check Parameters
        if (disprove(file, language, temperature)) return null;

        // Create Transcription
        TranscriptionResult result = createTranscription(file, prompt, language, temperature);

        // Delete File
        file.deleteOnExit();

        // Return Transcription
        return result.getText();
    }

    // Getter
    public JsonNode getConfig() {
        return config;
    }

    public TranscriptionModel getModel() {
        return model;
    }

    public OpenAiService getService() {
        return service;
    }

    public BigDecimal calculatePrice(int seconds) {
        return model.calculateCost(seconds);
    }

    public BigDecimal calculatePrice(AudioFile audioFile) {
        return model.calculateCost(audioFile);
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.io.tmpdir"));
    }
}