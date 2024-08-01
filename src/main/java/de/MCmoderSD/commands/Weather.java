package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.utilities.other.OpenAI;
import org.json.JSONArray;
import org.json.JSONObject;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonUtility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Weather {

    // Attributes
    private final String url;
    private final String apiKey;
    private final boolean isNull;
    private final OpenAI openAI;
    private final String botName;
    private final int maxTokens;
    private final double temperature;

    // Constructor
    public Weather(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, OpenAI openAI, String botName) {

        // Syntax
        String syntax = "Syntax: " + commandHandler.getPrefix() + "weather <city>, <language>";

        // About
        String[] name = {"weather", "wetter"};
        String description = "Zeigt das Wetter in einer Stadt an. " + syntax;

        // Load API key
        JsonUtility jsonUtility = new JsonUtility();
        JsonNode config = jsonUtility.load("/api/OpenWeatherMap.json");

        // Load Config
        this.openAI = openAI;
        this.botName = botName;
        maxTokens = openAI.getConfig().get("maxTokens").asInt();
        temperature = 0;

        // Init Attributes
        isNull = config == null || openAI.getConfig().isNull();
        url = isNull ? null : config.get("url").asText();
        apiKey = isNull ? null : config.get("api_key").asText();
        if (isNull) System.err.println(BOLD + "OpenWeatherMap API missing" + UNBOLD);

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                if (isNull) return;

                String response;
                if (args.length < 1) response = syntax;
                else response = trimMessage(generateFormattedResponse(args));

                // Send message and log response
                chat.sendMessage(getChannel(event), response);
                mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }

    // Generate response
    private String generateFormattedResponse(String... args) {

        // Split Input
        String language = "de"; // Default
        var parts = 0;
        for (String arg : args) {
            if (arg.contains(",")) break;
            if (!arg.isBlank() || !arg.isEmpty()) parts++;
        }

        // Remove comma
        for (var i = 0; i < args.length; i++) args[i] = args[i].replace(",", "");

        // Check language
        if (parts + 1 < args.length) {
            StringBuilder lang = new StringBuilder();
            for (var i = parts + 1; i < args.length; i++) lang.append(args[i]).append(" ");
            language = lang.toString();
        }

        // Build city name
        StringBuilder cityName = new StringBuilder();
        for (var i = 0; i < parts; i++) cityName.append(args[i]).append(" ");
        while (cityName.charAt(cityName.length() - 1) == ' ') cityName.deleteCharAt(cityName.length() - 1);
        String finalCityName = cityName.toString();

        // Query weather data
        String response = query(finalCityName);
        if (response == null || response.isEmpty() || response.isBlank()) return "Fehler beim Abrufen der Wetterdaten.";
        String formattedWeatherData = formatWeatherData(finalCityName, response);

        return openAI.prompt(botName, "Please format in short text and translate in: " + language, formattedWeatherData, maxTokens, temperature);
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