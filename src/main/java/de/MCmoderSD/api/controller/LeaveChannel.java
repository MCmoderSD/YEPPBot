package de.MCmoderSD.api.controller;

import de.MCmoderSD.api.objects.ApiResponse;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.objects.TwitchUser;

public class LeaveChannel extends Controller {

    // Endpoint
    public static final String ENDPOINT = "LeaveChannel";

    // Constructor
    public LeaveChannel(TwitchBot twitchBot, String key) {
        super(twitchBot, ENDPOINT, key);
    }

    // Execute Controller
    @Override
    protected ApiResponse execute(TwitchUser channel) {

        // Check if Joined
        if (!twitchBot.isChannelJoined(channel)) {
            twitchBot.getChannelManager().leaveChannel(channel);
            return ApiResponse.ok("Already left channel: " + channel.getDisplayName());
        }

        // Leave Channel
        if (twitchBot.leaveChannel(channel)) return ApiResponse.ok("Left channel: " + channel.getDisplayName());
        else return ApiResponse.internalError("Failed to leave channel: " + channel.getDisplayName());
    }
}