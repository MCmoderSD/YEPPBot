package de.MCmoderSD.utilities.OpenAI.enums;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public enum ImageModel {

    // Models
    DALL_E_2("dall-e-2", Resolution.RES_256x256, Resolution.RES_512x512, Resolution.RES_1024x1024),
    DALL_E_3("dall-e-3", Resolution.RES_1024x1024, Resolution.RES_1024x1792, Resolution.RES_1792x1024);

    // Attributes
    private final HashSet<String> models;
    private final HashSet<Resolution> resolutions;
    private final int minCharacters;
    private final int maxCharacters;
    private final int minAmount;
    private final int maxAmount;
    private final HashSet<Style> style;
    private final String model;

    // Constructor
    ImageModel(String model, Resolution... resolutions) {

        // Initialize attributes
        models = new HashSet<>();
        this.resolutions = new HashSet<>();
        style = new HashSet<>();
        minAmount = 1;
        maxAmount = 10;

        // Set models
        models.add("dall-e-2");
        models.add("dall-e-3");

        // Set Attributes
        if (models.contains(model)) this.model = model;
        else throw new IllegalArgumentException("Invalid model");

        // Set Resolutions
        Collections.addAll(this.resolutions, resolutions);

        if (model.equals("dall-e-2")) {
            minCharacters = 0;
            maxCharacters = 1000;
        } else {
            minCharacters = 0;
            maxCharacters = 4000;
        }

        // Set Style
        if (model.equals("dall-e-2")) return;
        style.add(Style.VIVID);
        style.add(Style.NATURAL);
    }

    // Getter
    public HashSet<String> getModels() {
        return models;
    }

    public HashSet<Resolution> getResolutions() {
        return resolutions;
    }

    public String getModel() {
        return model;
    }

    public HashSet<Style> getStyle() {
        return style;
    }

    public BigDecimal getPrice(Resolution resolution, Quality quality) {
        return resolution.getPrice(this, quality);
    }

    // Check Resolution
    public boolean checkModel(String model) {
        return models.contains(model);
    }

    public boolean checkPrompt(String prompt) {
        return prompt.length() >= minCharacters && prompt.length() <= maxCharacters;
    }

    public boolean checkAmount(int amount) {
        return amount >= minAmount && amount <= maxAmount;
    }

    public boolean checkQuality(String quality) {
        if (model.equals("dall-e-2") && quality.equals("standard")) return true;
        else if (model.equals("dall-e-3")) return quality.equals("standard") || quality.equals("hd");
        else return false;
    }

    public boolean checkResolution(String resolution) {
        return resolutions.stream().anyMatch(res -> res.checkResolution(resolution));
    }

    public boolean checkStyle(String style) {
        if (model.equals("dall-e-2")) return false;
        else return style.equals("vivid") || style.equals("natural");
    }

    public enum Resolution {

        // Resolutions
        RES_256x256("256x256", 0.016),
        RES_512x512("512x512", 0.018),
        RES_1024x1024("1024x1024", 0.04, 0.08),
        RES_1024x1792("1024x1792", 0.08, 0.12),
        RES_1792x1024("1792x1024", 0.12, 0.12);

        // Attributes
        private final HashSet<String> resolutions;
        private final HashSet<Quality> qualities;
        private final String resolution;
        private final BigDecimal standardPrice;
        private final BigDecimal hdPrice;

        // Constructor
        Resolution(String resolution, double standardPrice) {

            // Initialize attributes
            resolutions = new HashSet<>();
            qualities = new HashSet<>();

            // Set resolutions
            resolutions.add("256x256");
            resolutions.add("512x512");
            resolutions.add("1024x1024");
            resolutions.add("1024x1792");
            resolutions.add("1792x1024");

            // Set qualities
            qualities.add(Quality.STANDARD);

            // Set Attributes
            if (resolutions.contains(resolution)) this.resolution = resolution;
            else throw new IllegalArgumentException("Invalid resolution");

            // Set Prices
            this.standardPrice = new BigDecimal(standardPrice);
            hdPrice = null;
        }

        // Constructor
        Resolution(String resolution, double standardPrice, double hdPrice) {

            // Initialize attributes
            resolutions = new HashSet<>();
            qualities = new HashSet<>();

            // Set resolutions
            resolutions.add("256x256");
            resolutions.add("512x512");
            resolutions.add("1024x1024");
            resolutions.add("1024x1792");
            resolutions.add("1792x1024");

            // Set qualities
            qualities.add(Quality.STANDARD);
            qualities.add(Quality.HD);

            // Set Attributes
            if (resolutions.contains(resolution)) this.resolution = resolution;
            else throw new IllegalArgumentException("Invalid resolution");

            // Set Prices
            this.standardPrice = new BigDecimal(standardPrice);
            this.hdPrice = new BigDecimal(hdPrice);
        }

        // Getter
        public String getResolution() {
            return resolution;
        }

        public HashSet<String> getResolutions() {
            return resolutions;
        }

        public HashSet<Quality> getQualities() {
            return qualities;
        }

        public boolean checkResolution(String resolution) {
            return resolutions.contains(resolution);
        }

        public BigDecimal getPrice(ImageModel model, Quality quality) {
            if (model == ImageModel.DALL_E_2) {
                if (quality == Quality.STANDARD) return standardPrice;
                else throw new IllegalArgumentException("Invalid quality");
            } else throw new IllegalArgumentException("Invalid model");
        }
    }

    public enum Quality {

        // Qualities
        STANDARD("standard"),
        HD("hd");

        // Attributes
        private final HashSet<String> qualities;
        private final String quality;

        // Constructor
        Quality(String quality) {

            // Initialize attributes
            qualities = new HashSet<>();

            // Set qualities
            qualities.add("standard");
            qualities.add("hd");

            // Set Attributes
            if (qualities.contains(quality)) this.quality = quality;
            else throw new IllegalArgumentException("Invalid quality");
        }

        // Getter
        public HashSet<String> getQualities() {
            return qualities;
        }

        public String getQuality() {
            return quality;
        }

        public boolean checkQuality(String quality) {
            return qualities.contains(quality);
        }
    }

    public enum Style {

        // Styles
        VIVID("vivid"),
        NATURAL("natural");

        // Attributes
        private final HashSet<String> styles;
        private final String style;

        // Constructor
        Style(String style) {

            // Initialize attributes
            styles = new HashSet<>();

            // Set styles
            styles.add("vivid");
            styles.add("natural");

            // Set Attributes
            if (styles.contains(style)) this.style = style;
            else throw new IllegalArgumentException("Invalid style");
        }

        // Getter
        public HashSet<String> getStyles() {
            return styles;
        }

        public String getStyle() {
            return style;
        }

        public boolean checkStyle(String style) {
            return styles.contains(style);
        }
    }
}