import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.enums.SubTier;
import de.MCmoderSD.objects.TwitchMessageEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static java.nio.file.Files.readAllLines;

public class GenerateUUID{

    // Change Encryption Token
    public static void main(String[] args) throws IOException {

        // Set up variables
        BotClient.botNames = "YEPPBotV2, YEPPBot, YAPPBot, YEPBot, YAPBot".split(", ");
        BotClient.botName = BotClient.botNames[0];
        BotClient.prefixes = "! ¡ ? ¿".split(" ");

        // Load Data
        ArrayList<String> input = new ArrayList<>(Files.readAllLines(Path.of("MessageLog.tsv"), StandardCharsets.UTF_8));
        ArrayList<String> users = new ArrayList<>(readAllLines(Path.of("Users.tsv"), StandardCharsets.UTF_8));

        // Variables
        ArrayList<String> output = new ArrayList<>();
        ArrayList<String> events = new ArrayList<>();
        HashMap<Integer, String> userList = new HashMap<>();

        // Parse the users file
        for (String line : users) {
            String[] parts = line.split("\t");
            userList.put(Integer.parseInt(parts[0]), parts[1]);
        }

        System.out.println("Input: " + input.size());

        // Generate UUIDs for each line in the input
        for (String line : input) {
            output.add(UUID.randomUUID() + "\t" + line);
            if (output.size() % 1000 == 0) System.out.println("Done: " + output.size() + " / " + input.size());
        }

        System.out.println("Output: " + output.size());

        // Write the output to a file
        Files.write(Path.of("MessageLogUUID.tsv"), output, StandardCharsets.UTF_8);

        // Generate EventLogs
        for (String line : output) {

            // Parse the line
            String[] parts = line.split("\t");
            String eventId = parts[0];
            String timestamp = parts[1];
            Integer channelId = Integer.valueOf(parts[2]);
            Integer userId = Integer.valueOf(parts[3]);
            String channel = userList.get(channelId);
            String user = userList.get(userId);
            String message = parts[4];
            String bits = parts[5];
            String subMonths = parts[6];
            String subTier = parts[7];

            // Create TwitchMessageEvent
            TwitchMessageEvent event = new TwitchMessageEvent(
                    eventId,
                    Timestamp.valueOf(timestamp),
                    channelId,
                    userId,
                    channel,
                    user,
                    message,
                    Integer.parseInt(subMonths),
                    SubTier.valueOf(subTier),
                    Integer.parseInt(bits)
            );

            // Serialize the event
            events.add(line.split("\t")[0] + "\t" + bytesToHex(event.getBytes()));

            // Print progress
            if (events.size() % 1000 == 0) System.out.println("Done: " + events.size() + " / " + output.size());
        }

        System.out.println("Events: " + events.size());

        // Write the events to a file
        Files.write(Path.of("EventLog.tsv"), events, StandardCharsets.UTF_8);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}