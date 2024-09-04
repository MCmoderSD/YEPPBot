package de.MCmoderSD.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.domain.BitsLeaderboard;
import com.github.twitch4j.helix.domain.Moderator;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.ModeratorList;
import com.github.twitch4j.helix.domain.UserList;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.TokenManager;
import de.MCmoderSD.utilities.other.Encryption;
import de.MCmoderSD.utilities.server.Server;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class HelixHandler {

    // Constants
    private static final String AUTH_URL = "https://id.twitch.tv/oauth2/authorize";
    private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";

    // Attributes
    private final String hostname;
    private final int port;

    // Credentials
    private final String clientId;
    private final String clientSecret;

    // Associations
    private final BotClient botClient;
    private final TokenManager tokenManager;

    // Utilites
    private final Encryption encryption;

    // Constructor
    public HelixHandler(BotClient botClient, MySQL mySQL, Server server) {

        // Set Associations
        this.botClient = botClient;
        tokenManager = mySQL.getTokenManager();

        // Set Credentials
        JsonNode botConfig = botClient.getConfig();
        clientId = botConfig.get("clientId").asText();
        clientSecret = botConfig.get("clientSecret").asText();

        // Set Utilities
        encryption = new Encryption(botConfig);

        // Set Constants
        hostname = server.getHostname();
        port = server.getPort();

        // Init Server Context
        server.getHttpServer().createContext("/callback", new CallbackHandler());
    }

    // Send request
    private static HttpResponse<String> sendRequest(HttpRequest request) {
        try {
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to send request: " + e.getMessage());
            return null;
        }
    }

    // Refresh token
    public void refreshToken(String refreshToken) throws JsonProcessingException {

        // Create body
        String body = String.format(
                "client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                clientId, clientSecret, refreshToken
        );

        // Create request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // Send request
        HttpResponse<String> response = sendRequest(request);
        JsonNode jsonNode = new ObjectMapper().readTree(Objects.requireNonNull(response).body());
        String newAccessToken = encryption.encrypt(jsonNode.get("access_token").asText());
        String newRefreshToken = encryption.encrypt(jsonNode.get("refresh_token").asText());
        int expiresIn = jsonNode.get("expires_in").asInt();

        // Update tokens in the database
        tokenManager.refreshTokens(encryption.encrypt(refreshToken), newAccessToken, newRefreshToken, expiresIn);
    }

    // Get authorization URL
    public String getAuthorizationUrl(Scope... scopes) {
        StringBuilder scopeBuilder = new StringBuilder();
        for (Scope scope : Set.of(scopes)) scopeBuilder.append(scope.getScope()).append("+");
        return String.format(
                "%s?client_id=%s&redirect_uri=https://%s:%d/callback&response_type=code&scope=%s",
                AUTH_URL,
                clientId,
                hostname,
                port,
                scopeBuilder.substring(0, scopeBuilder.length() - 1)
        );
    }

    // Get bits leader board
    @SuppressWarnings("deprecation")
    public BitsLeaderboard getBitsLeaderboard(Integer channelId) {

        // Get access token
        boolean validScope = tokenManager.hasScope(channelId, Scope.BITS_READ);
        String accessToken = tokenManager.getAccessToken(channelId);

        // Decrypt access token
        if (validScope || accessToken == null) return null;
        else accessToken = encryption.decrypt(accessToken);

        // Create credential
        OAuth2Credential credential = new OAuth2Credential("twitch", accessToken);
        TwitchClient client = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withDefaultAuthToken(credential)
                .build();

        // Get bits leaderboard
        return client.getHelix().getBitsLeaderboard(accessToken, channelId.toString(), null, null, null).execute();
    }

    // Get moderators
    public Set<TwitchUser> getModerators(Integer channelId) {

        // Get access token
        boolean validScope = tokenManager.hasScope(channelId, Scope.MODERATION_READ);
        String accessToken = tokenManager.getAccessToken(channelId);

        // Decrypt access token
        if (validScope || accessToken == null) return null;
        else accessToken = encryption.decrypt(accessToken);

        // Create credential
        OAuth2Credential credential = new OAuth2Credential("twitch", accessToken);
        TwitchClient client = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withDefaultAuthToken(credential)
                .build();

        // Get moderators
        ModeratorList moderatorList = client.getHelix().getModerators(accessToken, channelId.toString(), null, null, null).execute();

        // Variables
        Set<TwitchUser> twitchUsers = new HashSet<>();

        // Add moderators
        for (Moderator moderator : moderatorList.getModerators()) twitchUsers.add(new TwitchUser(moderator));
        return twitchUsers;
    }

    // Callback handler
    private class CallbackHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // Extract code and scopes
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("code=")) {

                // Extract code and scopes
                String code = query.split("code=")[1].split("&")[0];
                String scopes = query.split("scope=")[1].split("&")[0];

                // Create body
                String body = String.format(
                        "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=https://%s:%d/callback",
                        clientId,
                        clientSecret,
                        code,
                        hostname,
                        port
                );

                // Create request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(TOKEN_URL))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                // Send request
                HttpResponse<String> response = sendRequest(request);
                JsonNode jsonNode = new ObjectMapper().readTree(Objects.requireNonNull(response).body());

                // Extract data
                String accessToken = jsonNode.get("access_token").asText();
                String refreshToken = jsonNode.get("refresh_token").asText();
                int expiresIn = jsonNode.get("expires_in").asInt();

                // Use the access token to determine the user
                if (accessToken != null) {
                    OAuth2Credential credential = new OAuth2Credential("twitch", accessToken);
                    TwitchClient client = TwitchClientBuilder.builder()
                            .withEnableHelix(true)
                            .withDefaultAuthToken(credential)
                            .build();

                    // Get user
                    UserList userList = client.getHelix().getUsers(null, null, null).execute();
                    if (!userList.getUsers().isEmpty()) {

                        // Extract user data
                        User user = userList.getUsers().getFirst();
                        int id = Integer.parseInt(user.getId());
                        String name = user.getDisplayName();

                        // Encrypt tokens
                        String encryptedAccessToken = encryption.encrypt(accessToken);
                        String encryptedRefreshToken = encryption.encrypt(refreshToken);

                        // Save token
                        tokenManager.addToken(id, name, encryptedAccessToken, encryptedRefreshToken, scopes, expiresIn);
                    }
                }
            }

            // Send response
            String response = "You can close this window now.";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }
    }

    // Enums
    public enum Scope {

        // Scopes
        ANALYTICS_READ_EXTENSIONS("analytics:read:extensions", "View analytics data for your extensions."),
        USER_EDIT("user:edit", "Manage a user object."),
        USER_READ_EMAIL("user:read:email", "Read authorized user's email address."),
        CLIPS_EDIT("clips:edit", "Create and edit clips as a specific user."),
        BITS_READ("bits:read", "View bits information for your channel."),
        ANALYTICS_READ_GAMES("analytics:read:games", "View analytics data for your games."),
        USER_EDIT_BROADCAST("user:edit:broadcast", "Edit your channel's broadcast configuration, including extension configuration."),
        USER_READ_BROADCAST("user:read:broadcast", "View your broadcasting configuration, including extension configurations."),
        CHAT_READ("chat:read", "View live Stream Chat and Rooms messages."),
        CHAT_EDIT("chat:edit", "Send live Stream Chat and Rooms messages."),
        CHANNEL_MODERATE("channel:moderate", "Perform moderation actions in a channel."),
        CHANNEL_READ_SUBSCRIPTIONS("channel:read:subscriptions", "Get a list of all subscribers to your channel and check if a user is subscribed to your channel."),
        WHISPERS_READ("whispers:read", "View your whisper messages."),
        WHISPERS_EDIT("whispers:edit", "Send whisper messages."),
        MODERATION_READ("moderation:read", "View your channel's moderation data including Moderators, Bans, Timeouts and Automod settings."),
        CHANNEL_READ_REDEMPTIONS("channel:read:redemptions", "View your channel points custom reward redemptions."),
        CHANNEL_EDIT_COMMERCIAL("channel:edit:commercial", "Run commercials on a channel."),
        CHANNEL_READ_HYPE_TRAIN("channel:read:hype_train", "View hype train data for a given channel."),
        CHANNEL_READ_STREAM_KEY("channel:read:stream_key", "Read authorized user's stream key."),
        CHANNEL_MANAGE_EXTENSIONS("channel:manage:extensions", "Manage your channel's extension configuration, including activating extensions."),
        CHANNEL_MANAGE_BROADCAST("channel:manage:broadcast", "Manage your channel's broadcast configuration, including updating channel configuration and managing stream markers and stream tags."),
        USER_EDIT_FOLLOWS("user:edit:follows", "Edit your follows."),
        CHANNEL_MANAGE_REDEMPTIONS("channel:manage:redemptions", "Manage Channel Points custom rewards and their redemptions on a channel."),
        CHANNEL_READ_EDITORS("channel:read:editors", "View a list of users with the editor role for a channel."),
        CHANNEL_MANAGE_VIDEOS("channel:manage:videos", "Manage a channel's videos, including deleting videos."),
        USER_READ_BLOCKED_USERS("user:read:blocked_users", "View the block list of a user."),
        USER_MANAGE_BLOCKED_USERS("user:manage:blocked_users", "Manage the block list of a user."),
        USER_READ_SUBSCRIPTIONS("user:read:subscriptions", "Get the details of your subscription to a channel."),
        USER_READ_FOLLOWS("user:read:follows", "View the list of channels a user follows."),
        CHANNEL_MANAGE_POLLS("channel:manage:polls", "Manage a channel's polls."),
        CHANNEL_MANAGE_PREDICTIONS("channel:manage:predictions", "Manage of channel's Channel Points Predictions"),
        CHANNEL_READ_POLLS("channel:read:polls", "View a channel's polls."),
        CHANNEL_READ_PREDICTIONS("channel:read:predictions", "View a channel's Channel Points Predictions."),
        MODERATOR_MANAGE_AUTOMOD("moderator:manage:automod", "Manage messages held for review by AutoMod in channels where you are a moderator."),
        CHANNEL_MANAGE_SCHEDULE("channel:manage:schedule", "Manage a channel's stream schedule."),
        CHANNEL_READ_GOALS("channel:read:goals", "View Creator Goals for a channel."),
        MODERATOR_READ_AUTOMOD_SETTINGS("moderator:read:automod_settings", "Read AutoMod settings in channels where you have the moderator role"),
        MODERATOR_MANAGE_AUTOMOD_SETTINGS("moderator:manage:automod_settings", "Manage AutoMod settings in channels where you have the moderator role"),
        MODERATOR_MANAGE_BANNED_USERS("moderator:manage:banned_users", "Ban or unban users in channels where you have the moderator role"),
        MODERATOR_READ_BLOCKED_TERMS("moderator:read:blocked_terms", "Read non-private blocked terms in channels where you have the moderator role"),
        MODERATOR_MANAGE_BLOCKED_TERMS("moderator:manage:blocked_terms", "Manage non-private blocked terms in channels where you have the moderator role"),
        MODERATOR_READ_CHAT_SETTINGS("moderator:read:chat_settings", "Read chat settings in channels where you have the moderator role"),
        MODERATOR_MANAGE_CHAT_SETTINGS("moderator:manage:chat_settings", "Manage chat settings in channels where you have the moderator role"),
        CHANNEL_MANAGE_RAIDS("channel:manage:raids", "Manage raids on your channel"),
        MODERATOR_MANAGE_ANNOUNCEMENTS("moderator:manage:announcements", "Send announcements in channels where you have the moderator role."),
        MODERATOR_MANAGE_CHAT_MESSAGES("moderator:manage:chat_messages", "Delete chat messages in channels where you have the moderator role"),
        USER_MANAGE_CHAT_COLOR("user:manage:chat_color", "Update the color used for the user's name in chat."),
        CHANNEL_MANAGE_MODERATORS("channel:manage:moderators", "Add or remove the moderator role from users in your channel."),
        CHANNEL_READ_VIPS("channel:read:vips", "Read the list of VIPs in your channel."),
        CHANNEL_MANAGE_VIPS("channel:manage:vips", "Add or remove the VIP role from users in your channel."),
        USER_MANAGE_WHISPERS("user:manage:whispers", "Read whispers that you send and receive, and send whispers on your behalf."),
        CHANNEL_READ_CHARITY("channel:read:charity", "Read charity campaign details and user donations on your channel."),
        MODERATOR_READ_CHATTERS("moderator:read:chatters", "Read the list of chatters in channels where you have the moderator role"),
        MODERATOR_READ_SHIELD_MODE("moderator:read:shield_mode", "Get information about Shield Mode and Shield Mode settings in channels where you have the moderator role"),
        MODERATOR_MANAGE_SHIELD_MODE("moderator:manage:shield_mode", "Manage Shield Mode and Shield Mode settings in channels where you have the moderator role"),
        MODERATOR_READ_SHOUTOUTS("moderator:read:shoutouts", "View a broadcaster's shoutouts."),
        MODERATOR_MANAGE_SHOUTOUTS("moderator:manage:shoutouts", "Manage a broadcaster's shoutouts."),
        MODERATOR_READ_FOLLOWERS("moderator:read:followers", "Read the list of followers in channels where you are a moderator."),
        CHANNEL_READ_GUEST_STAR("channel:read:guest_star", "Read Guest Star details for your channel."),
        CHANNEL_MANAGE_GUEST_STAR("channel:manage:guest_star", "Manage Guest Star for your channel."),
        MODERATOR_READ_GUEST_STAR("moderator:read:guest_star", "Read Guest Star details for channels where you are a Guest Star moderator."),
        MODERATOR_MANAGE_GUEST_STAR("moderator:manage:guest_star", "Manage Guest Star for channels where you are a Guest Star moderator."),
        CHANNEL_BOT("channel:bot", "Allows the client's bot users access to a channel."),
        USER_BOT("user:bot", "Allows client's bot to act as this user."),
        USER_READ_CHAT("user:read:chat", "View live stream chat and room messages."),
        CHANNEL_MANAGE_ADS("channel:manage:ads", "Manage ads schedule on a channel."),
        CHANNEL_READ_ADS("channel:read:ads", "Read the ads schedule and details on your channel."),
        USER_READ_MODERATED_CHANNELS("user:read:moderated_channels", "Read the list of channels you have moderator privileges in."),
        USER_WRITE_CHAT("user:write:chat", "Send live stream chat messages using Send Chat Message API."),
        USER_READ_EMOTES("user:read:emotes", "View emotes available to a user."),
        MODERATOR_READ_UNBAN_REQUESTS("moderator:read:unban_requests", "View a broadcaster’s unban requests."),
        MODERATOR_MANAGE_UNBAN_REQUESTS("moderator:manage:unban_requests", "Manage a broadcaster’s unban requests."),
        MODERATOR_READ_SUSPICIOUS_USERS("moderator:read:suspicious_users", "Read chat messages from suspicious users and see users flagged as suspicious in channels where you have the moderator role."),
        MODERATOR_MANAGE_WARNINGS("moderator:manage:warnings", "Warn users in channels where you have the moderator role.");

        // Attributes
        private final String scope;
        private final String description;

        // Constructor
        Scope(String scope, String description) {

            // Set Attributes
            this.scope = scope;
            this.description = description;
        }

        // Getters
        public String getScope() {
            return scope;
        }

        public String getDescription() {
            return description;
        }
    }
}