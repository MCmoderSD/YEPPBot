package de.MCmoderSD.utilities.OpenAI.enums;

import java.math.BigDecimal;
import java.util.HashSet;

@SuppressWarnings("unused")
public enum ChatModel {

    // Models
    GPT_4O("gpt-4o", 0.00500, 0.01500),
    GPT_4O_2024_08_06("gpt-4o-2024-08-06", 0.0025, 0.01),
    GPT_4O_2024_05_13("gpt-4o-2024-05-13", 0.005, 0.015),
    GPT_4O_MINI("gpt-4o-mini", 0.00015, 0.0006),
    GPT_4O_MINI_2024_07_18("gpt-4o-mini-2024-07-18", 0.00015, 0.0006);

    // Attributes
    private final HashSet<String> models;
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
    public HashSet<String> getModels() {
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