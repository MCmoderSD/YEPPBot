package de.MCmoderSD.core;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("unused")
public class TwitchAPI {

    private final String clientId;
    private final String clientSecret;

    public TwitchAPI(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public int getUserID(String username) {
        try {
            URI uri = new URI("https://api.twitch.tv/helix/users" + "?login=" + username);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Client-ID", clientId);
            connection.setRequestProperty("Authorization", "Bearer " + getOAuthToken(clientId, clientSecret));

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) response.append(inputLine);
            in.close();

            String jsonResponse = response.toString();
            var startIndex = jsonResponse.indexOf("\"id\":\"") + 6;
            var endIndex = jsonResponse.indexOf("\"", startIndex);
            var userID = jsonResponse.substring(startIndex, endIndex);
            if (userID.matches("[0-9]+")) return Integer.parseInt(userID);
        } catch (IOException | URISyntaxException e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    public String getOAuthToken(String clientId, String clientSecret) {
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://id.twitch.tv/oauth2/token");

            // Request parameters
            StringEntity params = new StringEntity("client_id=" + clientId + "&client_secret=" + clientSecret + "&grant_type=client_credentials");
            httpPost.setEntity(params);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity);

            var startIndex = jsonResponse.indexOf("\"access_token\":\"") + 16;
            var endIndex = jsonResponse.indexOf("\"", startIndex);
            return jsonResponse.substring(startIndex, endIndex);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}