package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;

import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.openweathermap.core.OpenWeatherMap;
import de.MCmoderSD.openweathermap.enums.SpeedUnit;
import de.MCmoderSD.openweathermap.enums.TempUnit;
import de.MCmoderSD.openweathermap.enums.TimeFormat;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Format.*;

public class Weather {

    // Constructor
    public Weather(BotClient botClient, MessageHandler messageHandler, OpenAI openAI, JsonNode apiConfig) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "weather <city>, <language>";

        // About
        String[] name = {"weather", "wetter"};
        String description = "Zeigt das Wetter in einer Stadt an. " + syntax;

        // Constants
        String errorRetrievingWeatherData = "Fehler beim Abrufen der Wetterdaten.";

        // Initialize OpenWeatherMap
        OpenWeatherMap openWeatherMap = new OpenWeatherMap(apiConfig.get("openWeatherMap").asText());

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                String response;
                if (args.isEmpty()) response = syntax;
                else {

                    // Split Input
                    String language = "de"; // Default
                    var parts = 0;
                    for (String arg : args) {
                        parts++;
                        if (arg.contains(",")) {
                            args.set(parts - 1, arg.replace(",", EMPTY));
                            break;
                        }
                    }

                    // Check language
                    if (parts < args.size()) {
                        StringBuilder lang = new StringBuilder();
                        for (var i = parts; i < args.size(); i++) lang.append(args.get(i)).append(SPACE);
                        language = lang.toString();
                    }

                    // Build city name
                    StringBuilder cityName = new StringBuilder();
                    for (var i = 0; i < parts; i++) cityName.append(args.get(i)).append(SPACE);
                    while (cityName.charAt(cityName.length() - 1) == ' ') cityName.deleteCharAt(cityName.length() - 1);
                    String finalCityName = cityName.toString();

                    // Query OpenWeatherMap
                    try {
                        de.MCmoderSD.openweathermap.data.Weather weather = openWeatherMap.query(finalCityName);
                        response = String.format(
                                "Wetter in %s: %s, bei %s°C (%s°C gefühlt), Luftfeuchtigkeit bei %s%%, Luftdruck bei %s hPa, Windgeschwindigkeit bei %s km/h , zu %s bewölkt, Sonnenaufgang: %s, Sonnenuntergang: %s (lokale Zeit)",
                                weather.getCity(),
                                weather.getDescription(),
                                weather.getTemperature(TempUnit.CELSIUS),
                                weather.getFeelsLike(TempUnit.CELSIUS),
                                weather.getHumidity(),
                                weather.getPressure(),
                                weather.getWindSpeed(SpeedUnit.KPH),
                                weather.getCloudiness(),
                                weather.getSunrise(TimeFormat.HH_MM_SS),
                                weather.getSunset(TimeFormat.HH_MM_SS));
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        response = errorRetrievingWeatherData;
                    }

                    // Translate
                    response = openAI.prompt(
                            null, 
                            event.getUser(),
                            null,
                            0d,
                            null,
                            null,
                            null,
                            null,
                            "Please format in short text and translate in: " + language,
                            response,
                            null
                    );
                }

                // Filter Response for argument injection
                response = removePrefix(response);

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }
}