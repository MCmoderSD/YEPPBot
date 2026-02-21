package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.openai.models.ChatModel;
import de.MCmoderSD.openai.services.ChatService;
import de.MCmoderSD.openweathermap.core.OpenWeatherMap;
import tools.jackson.databind.JsonNode;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static com.openai.models.ReasoningEffort.*;
import static de.MCmoderSD.openweathermap.enums.SpeedUnit.*;
import static de.MCmoderSD.openweathermap.enums.TempUnit.*;
import static de.MCmoderSD.utilities.MessageHelper.*;

public class Weather extends CommandBuilder {

    // API Config
    public static JsonNode api;

    // Constructor
    public Weather(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        String syntax = "Syntax: " + prefix + "Weather [city], [language]";

        // About
        String[] name = { "Weather", "Wetter", "Wetterbericht" };
        String description = "Zeigt den aktuellen Wetterbericht an. " + syntax;

        // Attributes
        ChatService chatService;
        OpenWeatherMap openWeatherMap;

        // Initialize OpenWeatherMap
        if (openAI == null || api == null) return;
        else {
            if (!api.has("apiKey") || api.get("apiKey").isNull() || !api.get("apiKey").isString()) throw new IllegalStateException("OpenWeatherMap API key is missing or invalid in the configuration.");
            openWeatherMap = new OpenWeatherMap(api.get("apiKey").asString());
        }

        // Initialize Chat Service
        chatService = ChatService.builder()
                .setModel(ChatModel.GPT_5_2)    // GPT-5.2
                .setReasoningEffort(NONE)       // Disable Reasoning
                .setTemperature(0)              // No randomness
                .setMaxOutputTokens(120)        // Limit response length
                .setInstructions(               // Custom instructions to format the weather data
                        """
                        Provide the weather data as a single concise plain text sentence in the specified language.
                        Keep the response under 500 characters without markdown or formatting.
                        """
                )
                .build(openAI);

        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Check Args
                if (args.isEmpty()) return twitchBot.sendMessage(event, name, "Bitte gib eine Stadt an. " + syntax);

                // Parse City and Language
                String argsString = String.join(SPACE, args);
                String[] parts =  argsString.split(",", 2);

                // Trim inputs
                String city = parts[0].trim();
                String language = parts.length > 1 ? parts[1].trim() : "German";

                // Fetch Weather Data
                String weatherData;
                try {
                    weatherData = formatWeatherResponse(openWeatherMap.query(city));
                } catch (IllegalArgumentException e) {
                    return twitchBot.sendMessage(event, name, "Für die angegebene Stadt '" + city + "' wurden keine Wetterdaten gefunden. Bitte überprüfe die Schreibweise oder versuche eine andere Stadt. YEPP");
                } catch (Exception e) {
                    return twitchBot.sendMessage(event, name, "Fehler beim Abrufen der Wetterdaten. Bitte versuche es später erneut. YEPP");
                }

                // Generate Response with ChatGPT
                String response = chatService.create("Please answer in " + language + ": " + weatherData).getContent();

                // Send Message
                return twitchBot.sendMessage(event, name, response);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    // Helper method to format weather data into a concise plain text string
    private static String formatWeatherResponse(de.MCmoderSD.openweathermap.data.Weather weather) {
        StringBuilder sb = new StringBuilder();
        sb.append("city=").append(weather.getCity()).append("\n");
        sb.append("weather=").append(weather.getTitle()).append("\n");
        sb.append("description=").append(weather.getDescription()).append("\n");
        sb.append("temperature=").append(weather.getTemperature(CELSIUS)).append("°C\n");
        sb.append("feelsLike=").append(weather.getFeelsLike(CELSIUS)).append("°C\n");
        sb.append("tempMin=").append(weather.getTempMin(CELSIUS)).append("°C\n");
        sb.append("tempMax=").append(weather.getTempMax(CELSIUS)).append("°C\n");
        sb.append("humidity=").append(weather.getHumidity()).append("%\n");
        sb.append("pressure=").append(weather.getPressure()).append(" hPa\n");
        sb.append("visibility=").append(weather.getVisibility()).append("\n");
        sb.append("windSpeed=").append(weather.getWindSpeed(KPH)).append(" km/h\n");
        sb.append("cloudiness=").append(weather.getCloudiness()).append("%\n");
        weather.getRain().ifPresent(rain -> sb.append("rain=").append(rain).append(" mm/h\n"));
        weather.getSnow().ifPresent(snow -> sb.append("snow=").append(snow).append(" mm/h\n"));
        sb.append("sunrise=").append(weather.getSunrise().atZone(weather.getTimezone().toZoneId()).format(DateTimeFormatter.ofPattern("HH:mm"))).append(" (local time)\n");
        sb.append("sunset=").append(weather.getSunset().atZone(weather.getTimezone().toZoneId()).format(DateTimeFormatter.ofPattern("HH:mm"))).append(" (local time)\n");
        return sb.toString();
    }
}