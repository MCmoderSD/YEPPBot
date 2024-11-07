package de.MCmoderSD.utilities.api;

import de.MCmoderSD.enums.ZodiacSign;
import de.MCmoderSD.objects.Birthdate;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import java.sql.Timestamp;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class ProkeralaAPI {

    // Constants
    private final String clientId;
    private final String clientSecret;

    // Variables
    private String accessToken;
    private Timestamp tokenExpiration;

    // Constructor
    public ProkeralaAPI(String clientId, String clientSecret) {

        // Set the client ID and client secret
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        // Initialize Variables
        accessToken = null;
        tokenExpiration = null;

        // Authenticate
        tokenExpiration = authenticate();
    }

    // Method to authenticate
    private Timestamp authenticate() {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpiration.getTime()) {
            try {

                // Create a new connection to the token endpoint
                URI uri = new URI("https://api.prokerala.com/token");
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                // Create the request body
                String requestBody = String.format(
                        "grant_type=client_credentials" +
                            "&client_id=%s" +
                            "&client_secret=%s",
                        clientId, clientSecret);

                // Send the request
                OutputStream os = conn.getOutputStream();
                os.write(requestBody.getBytes());
                os.flush();

                // Check the response code
                var responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    // Read the response
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);

                    // Parse the JSON response using org.json
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    accessToken = jsonResponse.getString("access_token");
                    var expiresIn = jsonResponse.getLong("expires_in");

                    // Set the token expiration time
                    return tokenExpiration = new Timestamp(System.currentTimeMillis() + expiresIn * 1000);
                }
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException("Failed to authenticate. " + e.getMessage());
            }
        }

        return tokenExpiration;
    }

    // Method to get the daily prediction
    public String dailyPrediction(Birthdate birthdate) {

        // Authenticate
        authenticate();
        if (accessToken == null) throw new RuntimeException("Failed to authenticate.");

        // Get the daily prediction
        String url = "https://api.prokerala.com/v2/horoscope/daily";
        ZodiacSign sign = birthdate.getZodiacSign();
        String currentDatetime = DateTimeFormatter.ISO_INSTANT.format(Instant.now().atOffset(ZoneOffset.UTC));
        String fullUrl = url + "?sign=" + sign.getName() + "&datetime=" + currentDatetime;

        try {

            // Create a new connection to the API
            URI requestUrl = new URI(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            // Check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();

                // Parse the JSON response using org.json
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse
                        .getJSONObject("data")
                        .getJSONObject("daily_prediction")
                        .getString("prediction");

            } else return "Failed to connect to the API. Response code: " + responseCode;
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to the API. " + e.getMessage());
        }
    }

    // Method to get the daily love prediction
    public String dailyLovePrediction(Birthdate user, Birthdate partner) {

        // Authenticate
        authenticate();
        if (accessToken == null) throw new RuntimeException("Failed to authenticate.");

        // Get the daily love prediction
        String url = "https://api.prokerala.com/v2/horoscope/daily/love-compatibility";
        String currentDatetime = DateTimeFormatter.ISO_INSTANT.format(Instant.now().atOffset(ZoneOffset.UTC));
        ZodiacSign userSign = user.getZodiacSign();
        ZodiacSign partnerSign = partner.getZodiacSign();
        String fullUrl = url + "?datetime=" + currentDatetime + "&sign_one=" + userSign.getName().toLowerCase() + "&sign_two=" + partnerSign.getName().toLowerCase();

        try {

            // Create a new connection to the API
            URI requestUrl = new URI(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            // Check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();

                // Parse the JSON response using org.json
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse
                        .getJSONObject("data")
                        .getJSONArray("daily_love_predictions")
                        .getJSONObject(0)
                        .getString("prediction");

            } else throw new RuntimeException("Failed to connect to the API. Response code: " + responseCode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to the API. " + e.getMessage());
        }
    }
}