package de.MCmoderSD.core;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.TwitchClientHelper;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.helix.TwitchHelix;
import de.MCmoderSD.helix.core.HelixHandler;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.other.Format;
import de.MCmoderSD.server.Server;

import static de.MCmoderSD.helix.core.HelixHandler.PROVIDER;

public class BotClient {

    // Attributes
    private final TwitchClient client;
    private final CredentialManager credentialManager;
    private final Server server;

    // Elements
    private final TwitchChat chat;
    private final TwitchHelix helix;
    private final TwitchClientHelper helper;
    private final EventManager eventManager;

    // Handler
    private final HelixHandler helixHandler;

    // Constructor
    public BotClient(Server server) {

        var clientId = "your-client-id";
        var clientSecret = "your-client-secret";
        var oauthToken = "your-oauth-token";
        var host = "localhost";
        var port = 8000;
        var proxy = "your-proxy";

        // Init Server
        this.server = server;

        // Init OAuth
        OAuth2Credential defaultAuthToken = new OAuth2Credential(PROVIDER, oauthToken);

        // Init CredentialManager
        credentialManager = CredentialManagerBuilder.builder().build();
        credentialManager.addCredential(PROVIDER, defaultAuthToken);

        // Init Client
        client = TwitchClientBuilder.builder()
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withDefaultAuthToken(defaultAuthToken)
                .withCredentialManager(credentialManager)
                .withChatCommandsViaHelix(true)
                .withEnableHelix(true)
                .withEnableChat(true)
                .build();

        // Init
        chat = client.getChat();
        helix = client.getHelix();
        helper = client.getClientHelper();
        eventManager = client.getEventManager();

        // Init HelixHandler
        helixHandler = new HelixHandler(server, helix, credentialManager);
    }

    // Methods
    public boolean sendMessage(TwitchUser channel, String message) {

        // Normalize Message
        message = Format.normalizeMessage(message);

        boolean tooLong = message.length() > 500;
        boolean valid = !(message.isBlank() || tooLong);

        // Check
        if (!valid) {
            // ToDo: Show Error
            return false;
        }

        // Send Message
        boolean success = chat.sendMessage(channel.getUsername(), message);

        if (success) {

            // ToDo: Update Frame

            // ToDo: Print Console

            // ToDo: Log Message

        }

        // Return
        return success;
    }

    public boolean joinChannel(TwitchUser channel) {

        // Join Channel
        chat.joinChannel(channel.getUsername());

        // Check
        if (!chat.isChannelJoined(channel.getUsername())) {
            // ToDo: Show Error
            return false;
        }

        // ToDo: Enable Event Listener

        // ToDo: Register Broadcast

        // Return
        return true;
    }

    public boolean leaveChannel(TwitchUser channel) {

        // Leave Channel
        chat.leaveChannel(channel.getUsername());

        // Check
        if (chat.isChannelJoined(channel.getUsername())) {
            // ToDo: Show Error
            return false;
        }

        // ToDo: Unregister Broadcast

        // Return
        return true;
    }

    // Setter
    public void connect() {
        chat.connect();
    }

    public void disconnect() {
        chat.disconnect();
    }

    public void closeChat() {
        chat.close();
    }

    public void close() {
        closeChat();
        client.close();
    }
}