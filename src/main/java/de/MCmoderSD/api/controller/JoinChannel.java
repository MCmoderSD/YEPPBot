package de.MCmoderSD.api.controller;

import de.MCmoderSD.api.objects.ApiResponse;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.objects.TwitchUser;

public class JoinChannel extends Controller {

    // Endpoint
    public static final String ENDPOINT = "JoinChannel";

    // Constructor
    public JoinChannel(TwitchBot twitchBot, String key) {
        super(twitchBot, ENDPOINT, key);
    }

    // Execute Controller
    @Override
    protected ApiResponse execute(TwitchUser channel) {

        // Check if Already Joined
        if (twitchBot.isChannelJoined(channel)) return ApiResponse.ok("Already joined channel: " + channel.getDisplayName());

        // Join Channel
        if (twitchBot.joinChannel(channel)) return ApiResponse.ok("Joined channel: " + channel.getDisplayName());
        else return ApiResponse.internalError("Failed to join channel: " + channel.getDisplayName());
    }
}