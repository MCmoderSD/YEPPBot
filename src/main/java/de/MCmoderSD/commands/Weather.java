package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.main.Credentials;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.other.OpenAi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Weather {

    // Attributes
    private final String url;
    private final String apiKey;
    private final OpenAi openAi;
    private final String botName;
    private final double temperature;
    private final int maxTokens;
    private final double topP;
    private final double frequencyPenalty;
    private final double presencePenalty;


    // Constructor
    public Weather(BotClient botClient, MessageHandler messageHandler, OpenAi openAi, Credentials credentials) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "weather <city>, <language>";

        // About
        String[] name = {"weather", "wetter"};
        String description = "Zeigt das Wetter in einer Stadt an. " + syntax;

        // Load API key
        JsonNode config = credentials.getOpenWeatherMapConfig();

        // Load Config
        this.openAi = openAi;
        this.botName = botClient.getBotName();
        temperature = 0;
        maxTokens = openAi.getConfig().get("maxTokens").asInt();
        topP = openAi.getConfig().get("topP").asDouble();
        frequencyPenalty = openAi.getConfig().get("frequencyPenalty").asDouble();
        presencePenalty = openAi.getConfig().get("presencePenalty").asDouble();

        // Init Attributes
        url = config.get("url").asText();
        apiKey = config.get("api_key").asText();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                String response;
                if (args.isEmpty()) response = syntax;
                else response = trimMessage(generateFormattedResponse(args));

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }

    // Generate response
    private String generateFormattedResponse(ArrayList<String> args) {

        // Split Input
        String language = "de"; // Default
        var parts = 0;
        for (String arg : args) {
            parts++;
            if (arg.contains(",")) {
                args.set(parts - 1, arg.replace(",", ""));
                break;
            }
        }

        // Check language
        if (parts < args.size()) {
            StringBuilder lang = new StringBuilder();
            for (var i = parts; i < args.size(); i++) lang.append(args.get(i)).append(" ");
            language = lang.toString();
        }

        // Build city name
        StringBuilder cityName = new StringBuilder();
        for (var i = 0; i < parts; i++) cityName.append(args.get(i)).append(" ");
        while (cityName.charAt(cityName.length() - 1) == ' ') cityName.deleteCharAt(cityName.length() - 1);
        String finalCityName = cityName.toString();

        // Query weather data
        String response = query(convertToAscii(finalCityName));
        if (response == null || response.isEmpty() || response.isBlank()) return "Fehler beim Abrufen der Wetterdaten.";
        String formattedWeatherData = formatWeatherData(finalCityName, response);

        return openAi.prompt(botName, "Please format in short text and translate in: " + language, formattedWeatherData, temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
    }

    private String formatWeatherData(String cityName, String response){

        // Weather Description
        JSONObject data = new JSONObject(response);
        JSONArray weatherArray = data.getJSONArray("weather");
        JSONObject weather = weatherArray.getJSONObject(0);
        String weatherDescription = weather.getString("description");    // English

        // Main weather data
        JSONObject main = data.getJSONObject("main");
        var tempKelvin = main.getDouble("temp");                      // Kelvin
        var tempCelsius = tempKelvin - 273.15;                             // Celsius
        var tempRounded = Math.toIntExact(Math.round(tempCelsius));           // Rounded
        var humidity = main.getInt("humidity");                          // %
        var pressure = main.getInt("pressure");                          // hPa

        // Wind data
        JSONObject wind = data.getJSONObject("wind");
        var windSpeed = wind.getDouble("speed");                      // m/s

        // Clouds data
        JSONObject clouds = data.getJSONObject("clouds");
        var cloudiness = clouds.getInt("all");                           // %

        // Visibility data
        String visibilityString;
        BigDecimal visibility = data.getBigDecimal("visibility");  // Meter
        if (visibility.intValue() == 10000) visibilityString = "";
        else if (visibility.intValue() >= 1000 && visibility.intValue() % 100 == 0) {
            visibility = visibility.divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
            visibilityString = visibility + " km Sichtweite, ";
        } else {
            visibilityString = visibility + " m Sichtweite, ";
        }

        // Sunrise and sunset data
        JSONObject sys = data.getJSONObject("sys");
        var sunriseUnixTimestamp = sys.getLong("sunrise");              // Unix Timestamp
        var sunsetUnixTimestamp = sys.getLong("sunset");                // Unix Timestamp
        String sunrise = formatUnixTimestamp(sunriseUnixTimestamp); // HH:mm:ss
        String sunset = formatUnixTimestamp(sunsetUnixTimestamp);   // HH:mm:ss

        // Build response
        return "Wetter in " + cityName + ": " + weatherDescription + ", bei " + tempRounded + "°C, Luftfeuchtigkeit bei " + humidity + "%, Luftdruck bei " + pressure + " hPa, Windgeschwindigkeit bei " + windSpeed + " m/s , zu " + cloudiness + "% bewölkt, " + visibilityString + "Sonnenaufgang: " + sunrise + ", Sonnenuntergang: " + sunset + " (lokale Zeit)";
    }

    // Query weather data
    private String query(String cityName) {
        StringBuilder response = new StringBuilder();
        try {
            String encodedCityName = cityName.replace(" ", "+");
            while (encodedCityName.charAt(encodedCityName.length() - 1) == '+')
                encodedCityName = encodedCityName.trim();
            URI uri = new URI(this.url + encodedCityName + this.apiKey);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();
        } catch (IOException | URISyntaxException e) {
            System.err.println(e.getMessage());
            return null;
        }

        return response.toString();
    }
}