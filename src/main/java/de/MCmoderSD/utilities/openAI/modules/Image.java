package de.MCmoderSD.utilities.openAI.modules;

import com.fasterxml.jackson.databind.JsonNode;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.service.OpenAiService;
import de.MCmoderSD.utilities.openAI.enums.ImageModel;

import java.math.BigDecimal;
import java.util.HashSet;

@SuppressWarnings({"unused", "UnusedReturnValue", "ClassCanBeRecord"})
public class Image {

    // Associations
    private final ImageModel model;
    private final OpenAiService service;

    // Attributes
    private final JsonNode config;

    // Constructor
    public Image(ImageModel model, JsonNode config, OpenAiService service) {

        // Set Associations
        this.model = model;
        this.config = config;
        this.service = service;
    }

    // Create Speech
    private ImageResult imageRequest(String user, String prompt, int amount, String quality, String resolution, String style) {

        // Request
        CreateImageRequest request = CreateImageRequest
                .builder()                  // Builder
                .model(model.getModel())    // Model
                .user(user)                 // User
                .prompt(prompt)             // Prompt
                .n(amount)                  // Amount
                .quality(quality)           // Quality
                .size(resolution)           // Resolution
                .style(style)               // Style
                .build();                   // Build

        // Result
        return service.createImage(request);
    }

    private ImageResult imageRequest(String user, String prompt, int amount, String resolution) {

        // Request
        CreateImageRequest request = CreateImageRequest
                .builder()                  // Builder
                .model(model.getModel())    // Model
                .user(user)                 // User
                .prompt(prompt)             // Prompt
                .n(amount)                  // Amount
                .size(resolution)           // Resolution
                .build();                   // Build

        // Result
        return service.createImage(request);
    }

    // Check Parameters
    public boolean disprove(String user, String prompt, int amount, String quality, String resolution, String style) {

        // Check Username
        if (user == null) throw new IllegalArgumentException("User name is null");
        if (user.isEmpty() || user.isBlank()) throw new IllegalArgumentException("User name is empty");

        // Check Prompt
        if (prompt == null) throw new IllegalArgumentException("Prompt is null");
        if (prompt.isEmpty() || prompt.isBlank()) throw new IllegalArgumentException("Prompt is empty");

        // Check Variables
        if (!model.checkPrompt(prompt)) throw new IllegalArgumentException("Invalid prompt");
        if (!model.checkAmount(amount)) throw new IllegalArgumentException("Invalid amount");
        if (!model.checkQuality(quality)) throw new IllegalArgumentException("Invalid quality");
        if (!model.checkResolution(resolution)) throw new IllegalArgumentException("Invalid resolution");
        if (!model.checkStyle(style)) throw new IllegalArgumentException("Invalid style");

        // Disapprove parameters
        return false;
    }

    // Check Parameters
    public boolean disprove(String user, String prompt, int amount, String resolution) {

        // Check Username
        if (user == null) throw new IllegalArgumentException("User name is null");
        if (user.isEmpty() || user.isBlank()) throw new IllegalArgumentException("User name is empty");

        // Check Prompt
        if (prompt == null) throw new IllegalArgumentException("Prompt is null");
        if (prompt.isEmpty() || prompt.isBlank()) throw new IllegalArgumentException("Prompt is empty");

        // Check Variables
        if (!model.checkPrompt(prompt)) throw new IllegalArgumentException("Invalid prompt");
        if (!model.checkAmount(amount)) throw new IllegalArgumentException("Invalid amount");
        if (!model.checkResolution(resolution)) throw new IllegalArgumentException("Invalid resolution");

        // Disapprove parameters
        return false;
    }

    // Create Image
    public HashSet<String> generate(String user, String prompt, int amount, String quality, String resolution, String style) {

        // Approve parameters
        if (disprove(user, prompt, amount, quality, resolution, style))
            throw new IllegalArgumentException("Invalid parameters");

        // Request Image
        ImageResult result = imageRequest(user, prompt, amount, quality, resolution, style);

        // Return Images
        HashSet<String> images = new HashSet<>();
        result.getData().forEach(image -> images.add(image.getUrl()));
        return images;
    }

    public HashSet<String> generate(String user, String prompt, int amount, String resolution) {

        // Approve parameters
        if (disprove(user, prompt, amount, resolution))
            throw new IllegalArgumentException("Invalid parameters");

        // Request Image
        ImageResult result = imageRequest(user, prompt, amount, resolution);

        // Return Images
        HashSet<String> images = new HashSet<>();
        result.getData().forEach(image -> images.add(image.getUrl()));
        return images;
    }

    // Getter
    public JsonNode getConfig() {
        return config;
    }

    public ImageModel getModel() {
        return model;
    }

    public OpenAiService getService() {
        return service;
    }

    public BigDecimal calculatePrice(ImageModel.Resolution resolution, ImageModel.Quality quality) {
        return model.getPrice(resolution, quality);
    }
}
