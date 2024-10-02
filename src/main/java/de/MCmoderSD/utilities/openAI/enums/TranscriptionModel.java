package de.MCmoderSD.utilities.openAI.enums;

import de.MCmoderSD.objects.AudioFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashSet;

@SuppressWarnings("unused")
public enum TranscriptionModel {

    // Models
    WHISPER("whisper-1", 0.006);

    // Attributes
    private final HashSet<String> models;
    private final HashSet<String> languages;
    private final String model;
    private final double minTemperature;
    private final double maxTemperature;
    private final BigDecimal uploadLimit;
    private final BigDecimal price;

    // Constructor
    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    TranscriptionModel(String model, double price) {

        // Initialize attributes
        models = new HashSet<>();
        languages = new HashSet<>();

        // Set models
        models.add("whisper-1");

        // Set languages
        // Set languages in ISO-639-1 format
        languages.add("af"); // Afrikaans
        languages.add("ar"); // Arabic
        languages.add("hy"); // Armenian
        languages.add("az"); // Azerbaijani
        languages.add("be"); // Belarusian
        languages.add("bs"); // Bosnian
        languages.add("bg"); // Bulgarian
        languages.add("ca"); // Catalan
        languages.add("zh"); // Chinese
        languages.add("hr"); // Croatian
        languages.add("cs"); // Czech
        languages.add("da"); // Danish
        languages.add("nl"); // Dutch
        languages.add("en"); // English
        languages.add("et"); // Estonian
        languages.add("fi"); // Finnish
        languages.add("fr"); // French
        languages.add("gl"); // Galician
        languages.add("de"); // German
        languages.add("el"); // Greek
        languages.add("he"); // Hebrew
        languages.add("hi"); // Hindi
        languages.add("hu"); // Hungarian
        languages.add("is"); // Icelandic
        languages.add("id"); // Indonesian
        languages.add("it"); // Italian
        languages.add("ja"); // Japanese
        languages.add("kn"); // Kannada
        languages.add("kk"); // Kazakh
        languages.add("ko"); // Korean
        languages.add("lv"); // Latvian
        languages.add("lt"); // Lithuanian
        languages.add("mk"); // Macedonian
        languages.add("ms"); // Malay
        languages.add("mr"); // Marathi
        languages.add("mi"); // Maori
        languages.add("ne"); // Nepali
        languages.add("no"); // Norwegian
        languages.add("fa"); // Persian
        languages.add("pl"); // Polish
        languages.add("pt"); // Portuguese
        languages.add("ro"); // Romanian
        languages.add("ru"); // Russian
        languages.add("sr"); // Serbian
        languages.add("sk"); // Slovak
        languages.add("sl"); // Slovenian
        languages.add("es"); // Spanish
        languages.add("sw"); // Swahili
        languages.add("sv"); // Swedish
        languages.add("tl"); // Tagalog
        languages.add("ta"); // Tamil
        languages.add("th"); // Thai
        languages.add("tr"); // Turkish
        languages.add("uk"); // Ukrainian
        languages.add("ur"); // Urdu
        languages.add("vi"); // Vietnamese
        languages.add("cy"); // Welsh

        // Set Attributes
        if (models.contains(model)) this.model = model;
        else throw new IllegalArgumentException("Invalid model");
        this.price = new BigDecimal(price).divide(new BigDecimal(60));
        this.uploadLimit = new BigDecimal(25).movePointRight(6);
        minTemperature = 0.0;
        maxTemperature = 2.0;
    }

    // Methods
    public BigDecimal calculateCost(int seconnds) {
        return price.multiply(new BigDecimal(seconnds));
    }

    public BigDecimal calculateCost(AudioFile input) {
        return calculateCost(input.getDuration());
    }

    // Getter
    public HashSet<String> getModels() {
        return models;
    }

    public HashSet<String> getLanguages() {
        return languages;
    }

    public BigDecimal getUploadLimit() {
        return uploadLimit;
    }

    public String getModel() {
        return model;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public BigDecimal getPrice() {
        return price;
    }

    // Check
    public boolean checkModel(String model) {
        return models.contains(model);
    }

    public boolean checkTemperature(double temperature) {
        return temperature >= minTemperature && temperature <= maxTemperature;
    }

    public boolean checkSize(File input) {
        if (!input.exists() || !input.isFile()) return false;
        return input.length() < uploadLimit.longValue();
    }

    public boolean checkInput(File input) {
        return input.length() < uploadLimit.longValue();
    }

    public boolean checkInput(AudioFile input) {
        return input.getSize() < uploadLimit.longValue();
    }

    public boolean checkLanguage(String language) {
        return languages.contains(language.toLowerCase());
    }
}