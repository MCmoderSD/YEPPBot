package de.MCmoderSD.main;

import de.MCmoderSD.enums.Argument;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

import static de.MCmoderSD.utilities.other.Format.*;

public class Terminal {

    // Constants
    public final static long startTime = System.nanoTime();
    public final static String ICON =
            """
            ⠄⠄⠄⠄⠄⠄⠄⣠⣴⣶⣿⣿⡿⠶⠄⠄⠄⠄⠐⠒⠒⠲⠶⢄⠄⠄⠄⠄⠄⠄
            ⠄⠄⠄⠄⠄⣠⣾⡿⠟⠋⠁⠄⢀⣀⡀⠤⣦⢰⣤⣶⢶⣤⣤⣈⣆⠄⠄⠄⠄⠄
            ⠄⠄⠄⠄⢰⠟⠁⠄⢀⣤⣶⣿⡿⠿⣿⣿⣊⡘⠲⣶⣷⣶⠶⠶⠶⠦⠤⡀⠄⠄
            ⠄⠔⠊⠁⠁⠄⠄⢾⡿⣟⡯⣖⠯⠽⠿⠛⠛⠭⠽⠊⣲⣬⠽⠟⠛⠛⠭⢵⣂⠄
            ⡎⠄⠄⠄⠄⠄⠄⠄⢙⡷⠋⣴⡆⠄⠐⠂⢸⣿⣿⡶⢱⣶⡇⠄⠐⠂⢹⣷⣶⠆
            ⡇⠄⠄⠄⠄⣀⣀⡀⠄⣿⡓⠮⣅⣀⣀⣐⣈⣭⠤⢖⣮⣭⣥⣀⣤⣤⣭⡵⠂⠄
            ⣤⡀⢠⣾⣿⣿⣿⣿⣷⢻⣿⣿⣶⣶⡶⢖⣢⣴⣿⣿⣟⣛⠿⠿⠟⣛⠉⠄⠄⠄
            ⣿⡗⣼⣿⣿⣿⣿⡿⢋⡘⠿⣿⣿⣷⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⡀⠄⠄
            ⣿⠱⢿⣿⣿⠿⢛⠰⣞⡛⠷⣬⣙⡛⠻⠿⠿⠿⣿⣿⣿⣿⣿⣿⣿⠿⠛⣓⡀⠄
            ⢡⣾⣷⢠⣶⣿⣿⣷⣌⡛⠷⣦⣍⣛⠻⠿⢿⣶⣶⣶⣦⣤⣴⣶⡶⠾⠿⠟⠁⠄
            ⣿⡟⣡⣿⣿⣿⣿⣿⣿⣿⣷⣦⣭⣙⡛⠓⠒⠶⠶⠶⠶⠶⠶⠶⠶⠿⠟⠄⠄⠄
            ⠿⡐⢬⣛⡻⠿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⡶⠟⠃⠄⠄⠄⠄⠄⠄
            ⣾⣿⣷⣶⣭⣝⣒⣒⠶⠬⠭⠭⠭⠭⠭⠭⠭⣐⣒⣤⣄⡀⠄⠄⠄⠄⠄⠄⠄⠄
            ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣦⠄⠄⠄⠄⠄⠄⠄""";

    // Attributes
    private final Scanner scanner;
    private final String[] args;
    private final HashSet<Argument> arguments;

    // Constructor
    public Terminal(String[] args) {

        // Print Start Message
        System.out.println(ICON);
        System.out.printf("%sYEPPBot v%s%s", BOLD, Main.VERSION, BREAK);

        // Init
        this.args = args;
        scanner = new Scanner(System.in);
        arguments = new HashSet<>();

        // Check Arguments
        for (String arg : args) {
            if (arg == null || arg.isBlank()) continue;
            String copy = arg;
            while (copy.startsWith("-") || copy.startsWith("/")) copy = copy.substring(1);
            for (Argument argument : Argument.values()) if (argument.hasNameOrAlias(copy)) this.arguments.add(argument);
        }

        // Host and Port
        if (hasArg(Argument.HOST)) args[0] = args[Arrays.asList(args).indexOf("-host") + 1];
        if (hasArg(Argument.PORT)) args[1] = args[Arrays.asList(args).indexOf("-port") + 1];

        // Start Input Loop
        new Thread(this::inputLoop).start();
    }

    private void inputLoop() {

        // Loop
        while (scanner.hasNext()) handleInput(scanner.nextLine());

        // Exit
        scanner.close();
        System.out.printf("%sStopping bot...", BOLD);
        System.exit(0);
    }

    private void handleInput(String input) {
        while (input.startsWith(" ")) input = input.substring(1);
        while (input.endsWith(" ")) input = input.substring(0, input.length() - 1);
        switch (input.toLowerCase().trim()) {
            case "exit", "stop" -> exit();
            case "clear", "cls", "clr" -> clear();
            case "gen", "generate" -> gen();
            case "help", "h", "?" -> help();
            case "uptime" -> System.out.println(calculateUptime());
            default -> System.out.println("Unknown Command: " + input);
        }
    }

    // Commands
    private void exit() {
        System.out.printf("%sStopping bot...", BOLD);
        System.exit(0);
    }

    private void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void gen() {
        System.out.println("Generating config files...");
        if (generateConfigFiles()) {
            System.out.println("Config files generated successfully");
            System.out.printf("%sStopping bot...", BOLD);
            System.exit(0);
        } else System.out.println("Error while generating config files");
    }

    private void help() {
        // Info
        System.out.println(
                """
                \n
                
                You can run the bot with the following arguments:
                
                Info:
                    -help: Show Help
                    -version: Show Version
                """);

        // Modes
        System.out.println(
                """ 
                Modes:
                    -dev: Development Mode
                    -cli: CLI Mode (No GUI)
                    -nolog: Disable Logging
                """);

        // Generate Config Files
        System.out.println(
                """ 
                Generate:
                    -generate: Generate Config Files
                """);

        // Bot Config
        System.out.println(
                """
                Bot Config:
                    -botconfig: Path to Bot Config
                    -channellist: Path to Channel List
                    -mysql: Path to MySQL Config
                    -httpsserver: Path to Https Server Config
                """);

        // API Config
        System.out.println(
                """
                API Config:
                    -api: Path to API Config
                    -openai: Path to OpenAI Config
                """);
    }

    // Methods
    private String calculateUptime() {

        // Get Uptime
        long uptime = System.nanoTime() - startTime;
        StringBuilder builder = new StringBuilder("Current Uptime: ");

        // Days
        long days = uptime / 86_400_000_000_000L;
        if (days > 0) {
            builder.append(days).append(" day");
            if (days > 1) builder.append("s");
        }

        // Hours
        long hours = (uptime % 86_400_000_000_000L) / 3_600_000_000_000L;
        if (hours > 0) {
            if (days > 0) builder.append(", ");
            builder.append(hours).append(" hour");
            if (hours > 1) builder.append("s");
        }

        // Minutes
        long minutes = (uptime % 3_600_000_000_000L) / 60_000_000_000L;
        if (minutes > 0) {
            if (days > 0 || hours > 0) builder.append(", ");
            builder.append(minutes).append(" minute");
            if (minutes > 1) builder.append("s");
        }

        // Seconds
        long seconds = (uptime % 60_000_000_000L) / 1_000_000_000L;
        if (seconds > 0) {
            if (days > 0 || hours > 0 || minutes > 0) builder.append(", ");
            builder.append(seconds).append(" second");
            if (seconds > 1) builder.append("s");
        }

        // Return
        return builder.toString();
    }

    private boolean generateConfigFiles() {

        // Files
        String[] fileNames = {"BotConfig.json", "Channel.list", "mySQL.json", "httpsServer.json", "apiKeys.json", "ChatGPT.json"};

        for (String fileName : fileNames) {
            try {

                // Input Stream
                InputStream inputStream = Main.class.getResourceAsStream("/examples/" + fileName);

                // Check
                if (inputStream == null) throw new RuntimeException("File not found: " + fileName);

                // Output Stream
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));

                // Read and Write
                byte[] data = inputStream.readAllBytes();
                bufferedWriter.write(new String(data));

                // Close
                inputStream.close();
                bufferedWriter.close();

            } catch (IOException e) {
                System.err.println("Error generating " + fileName + ": " + e.getMessage());
                return false;
            }
        }

        // Exit
        return true;
    }

    // Getters
    public boolean hasArg(Argument arg) {
        return arguments.contains(arg);
    }

    public String[] getArgs() {
        return args;
    }
}