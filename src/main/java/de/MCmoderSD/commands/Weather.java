package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.json.JsonUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Weather {

    // Attributes
    private final String url;
    private final String apiKey;

    // Constructor
    public Weather(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat) {

        // About
        String[] name = {"weather", "wetter"};
        String description = "Zeigt das Wetter in einer Stadt an. Verwendung: " + commandHandler.getPrefix() + "weather <Stadt>";


        // Load API key
        JsonUtility jsonUtility = new JsonUtility();
        JsonNode config = jsonUtility.load("/api/OpenWeatherMap.json");
        url = config.get("url").asText();
        apiKey = config.get("api_key").asText();

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                // Send message
                String response = trimMessage(generateResponse(args));
                chat.sendMessage(getChannel(event), response);

                // Log response
                if (mySQL != null) mySQL.logResponse(event, getCommand(), processArgs(args), response);
            }
        });
    }

    // Generate response
    private String generateResponse(String... args) {

        // Build city name
        StringBuilder cityName = new StringBuilder();
        Arrays.stream(args).forEach(arg -> cityName.append(arg).append(" "));
        while (cityName.charAt(cityName.length() - 1) == ' ') cityName.deleteCharAt(cityName.length() - 1);

        // Query weather data
        JSONObject jsonObject = query(cityName.toString());
        if (jsonObject == null) return "Fehler beim Abrufen der Wetterdaten.";
        JSONArray weatherArray = jsonObject.getJSONArray("weather");
        JSONObject weather = weatherArray.getJSONObject(0);

        // Weather Description
        String weatherDescription = weather.getString("description");    // English

        // Main weather data
        JSONObject main = jsonObject.getJSONObject("main");
        var tempKelvin = main.getDouble("temp");                      // Kelvin
        var tempCelsius = tempKelvin - 273.15;                             // Celsius
        var tempRounded = Math.toIntExact(Math.round(tempCelsius));           // Rounded
        var humidity = main.getInt("humidity");                          // %
        var pressure = main.getInt("pressure");                          // hPa

        // Wind data
        JSONObject wind = jsonObject.getJSONObject("wind");
        var windSpeed = wind.getDouble("speed");                      // m/s

        // Clouds data
        JSONObject clouds = jsonObject.getJSONObject("clouds");
        var cloudiness = clouds.getInt("all");                           // %

        // Visibility data
        String visibilityString;
        BigDecimal visibility = jsonObject.getBigDecimal("visibility");  // Meter
        if (visibility.intValue() == 10000) visibilityString = "";
        else if (visibility.intValue() >= 1000 && visibility.intValue() % 100 == 0) {
            visibility = visibility.divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
            visibilityString = visibility + " km Sichtweite, ";
        } else {
            visibilityString = visibility + " m Sichtweite, ";
        }

        // Sunrise and sunset data
        JSONObject sys = jsonObject.getJSONObject("sys");
        var sunriseUnixTimestamp = sys.getLong("sunrise");              // Unix Timestamp
        var sunsetUnixTimestamp = sys.getLong("sunset");                // Unix Timestamp
        String sunrise = formatUnixTimestamp(sunriseUnixTimestamp); // HH:mm:ss
        String sunset = formatUnixTimestamp(sunsetUnixTimestamp);   // HH:mm:ss

        // Build response
        return "Wetter in " + cityName + ": " + weatherDescription + ", bei " + tempRounded + "°C, Luftfeuchtigkeit bei " + humidity + "%, Luftdruck bei " + pressure + " hPa, Windgeschwindigkeit bei " + windSpeed + " m/s , zu " + cloudiness + "% bewölkt, " + visibilityString + "Sonnenaufgang: " + sunrise + ", Sonnenuntergang: " + sunset + " (lokale Zeit)";
    }

    // Query weather data
    private JSONObject query(String cityName) {
        StringBuilder response = new StringBuilder();
        try {
            String encodedCityName = cityName.replace(" ", "+");
            while (encodedCityName.charAt(encodedCityName.length() - 1) == '+')
                encodedCityName = encodedCityName.trim();
            URL url = new URL(this.url + encodedCityName + this.apiKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }

        return new JSONObject(response.toString());
    }
}