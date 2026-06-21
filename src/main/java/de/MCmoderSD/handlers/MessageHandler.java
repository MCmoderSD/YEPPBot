package de.MCmoderSD.handlers;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.database.manager.OpenAIManger;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.openai.prompts.ChatPrompt;
import de.MCmoderSD.openai.services.ChatService;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import static com.openai.models.ReasoningEffort.*;
import static de.MCmoderSD.openai.models.ChatModel.*;
import static de.MCmoderSD.utilities.MessageHelper.*;

public class MessageHandler {

    // Associations
    private final TwitchBot twitchBot;

    // Handlers
    private final BirthdayHandler birthdayHandler;
    private final LurkHandler lurkHandler;
    private final CommandHandler commandHandler;

    // Database
    private final OpenAIManger openAIManger;

    // OpenAI Service
    private final ChatService service;

    // Attributes
    private final TwitchUser botUser;
    private final HashSet<String> botAliases;
    private final ArrayList<String> prefixes;
    private final ConcurrentHashMap<TwitchUser, String> conversations;

    // Constructor
    public MessageHandler(TwitchBot twitchBot) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Associations
        this.twitchBot = twitchBot;

        // Initialize Handlers
        birthdayHandler = new BirthdayHandler(twitchBot);
        lurkHandler = new LurkHandler(twitchBot);
        commandHandler = new CommandHandler(twitchBot);

        // Set Database
        openAIManger = twitchBot.getOpenAIManger();

        // Initialize OpenAI Service
        var openAI = twitchBot.getOpenAI();
        if (openAI == null) service = null;
        else service = ChatService.builder()
                .setModel(GPT_5_4_NANO)
                .setReasoningEffort(NONE)
                .setMaxOutputTokens(80)
                .setInstructions(
                        """
                        You are a TwitchBot called YEPPBot.
                        Express yourself like a typical Twitch user, consistently using the YEPP in your sentences, especially at the end of each message.
                        Do not use standard emojis, only use common Twitch emotes, with a preference for the YEPP.
                        Keep your response short, less then 500 characters.
                        """
                )
                .build(twitchBot.getOpenAI());

        // Initialize Attributes
        botUser = twitchBot.getBotUser();
        botAliases = twitchBot.getBotAliases();
        prefixes = twitchBot.getPrefixes();
        conversations = new ConcurrentHashMap<>(openAIManger.getConversations());
    }

    // Check if Message is Command
    private boolean isCommand(String message) {
        for (var prefix : prefixes) if (message.startsWith(prefix)) return true;
        for (var prefix : prefixes) if (message.contains(SPACE + prefix)) return true;
        return false;
    }

    // Check if Message Mentions Bot
    private boolean mentionsBot(String message) {
        message = message.toLowerCase();
        for (var alias : botAliases) if (message.contains(alias.toLowerCase())) return true;
        return message.contains(botUser.getUsername());
    }

    // Handle Message
    @SuppressWarnings("UnusedReturnValue")
    public boolean handleMessage(MessageEvent event) {

        // Check Parameters
        if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");

        // Handle Birthday
        birthdayHandler.handleBirthday(event);

        // Handle Lurk
        lurkHandler.handleLurk(event);

        // Variables
        var message = event.getMessage();

        // Handle Command
        if (isCommand(message)) return commandHandler.handleCommand(event);

        // Handle YEPP
        if (mentionsBot(message)) {

            // Variables
            var user = event.getUser();

            // Check if OpenAI Service Available
            if (service == null) return twitchBot.sendMessage(event, YEPP, tagUser(user) + SPACE + YEPP);

            // Create Prompt
            var prompt = conversations.containsKey(user) ? service.create(message, conversations.get(user)) : service.create(message);

            // Update Conversations
            updateConversation(user, prompt);

            // Send Response
            return twitchBot.sendMessage(event, "AI-Reply", tagUser(user) + SPACE + formatOpenAI(prompt.getContent()));

        } else if (message.toUpperCase().contains("YEP")) return twitchBot.sendMessage(event, YEPP, YEPP);

        // Default
        return true;
    }

    // Update Conversation
    public void updateConversation(TwitchUser user, @Nullable ChatPrompt prompt) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");

        // Check if Conversation needs to be reset due to token limit
        var reset = prompt == null || prompt.getInputTokens() > 16348;

        // Update Conversations
        if (reset) conversations.remove(user);
        else conversations.put(user, prompt.getId());

        // Update Database
        if (reset) openAIManger.deleteConversation(user);
        else openAIManger.saveConversation(user, prompt);
    }

    // Get Conversation
    public String getConversation(TwitchUser user) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");

        // Get Conversation
        return conversations.getOrDefault(user, null);
    }

    // Getter
    public BirthdayHandler getBirthdayHandler() {
        return birthdayHandler;
    }

    public LurkHandler getLurkHandler() {
        return lurkHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }
}