package de.MCmoderSD.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

import static de.MCmoderSD.utilities.other.Format.*;

public class Terminal {

    // Constants
    public final static long startTime = System.nanoTime();

    // Attributes
    private final Scanner scanner;
    private final String[] args;
    private final HashSet<Argument> arguments;

    // Constructor
    public Terminal(String[] args) {

        // Print Start Message
        System.out.println("""
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
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣦⠄⠄⠄⠄⠄⠄⠄""");

        System.out.printf("%sYEPPBot v%s%s", BOLD, Main.VERSION, BREAK);

        // Init
        scanner = new Scanner(System.in);
        this.args = args;
        arguments = new HashSet<>();

        // Check Arguments
        for (String arg : args) {
            if (arg == null || arg.isEmpty() || arg.isBlank()) continue;
            while (arg.startsWith("-") || arg.startsWith("/")) arg = arg.substring(1);
            for (Argument argument : Argument.values()) if (argument.hasNameOrAlias(arg)) this.arguments.add(argument);
        }

        // Host and Port
        if (hasArg(Argument.HOST)) args[0] = args[Arrays.asList(args).indexOf("-host") + 1];
        if (hasArg(Argument.PORT)) args[1] = args[Arrays.asList(args).indexOf("-port") + 1];

        // Start Input Loop
        new Thread(this::inputLoop).start();
    }

    private void handleInput(String input) {
        switch (input) {
            case "help":
                help();
                break;
            case "exit":
                System.out.printf("%sStopping bot...", BOLD);
                System.exit(0);
                break;
            case "version":
                System.out.println("Version: " + Main.VERSION);
                break;
            case "uptime":
                System.out.println(uptime());
                break;
            case "generate":
            case "gen":
                System.out.println("Generating config files...");
                if (generateConfigFiles()) {
                    System.out.println("Config files generated successfully");
                    System.out.printf("%sStopping bot...", BOLD);
                    System.exit(0);
                } else System.out.println("Error while generating config files");
                break;
            default:
                System.out.println("Unknown command. Type help for a list of commands.");
                break;
        }
    }

    private void inputLoop() {

        // Loop
        while (scanner.hasNext()) {

            // Get Input
            String input = scanner.nextLine().toLowerCase();

            // Handle Input
            handleInput(input);
        }

        // Close Scanner
        scanner.close();

        // Exit
        System.out.printf("%sStopping bot...", BOLD);
        System.exit(0);
    }

    // Help
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
                    -mysqlconfig: Path to MySQL Config
                    -httpsserver: Path to Https Server Config
                """);

        // API Config
        System.out.println(
                """
                API Config:
                    -apiconfig: Path to API Config
                    -openaiconfig: Path to OpenAI Config
                """);
    }

    // Generate Config Files
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

    private String uptime() {

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

    public boolean hasArg(Argument arg) {
        return arguments.contains(arg);
    }

    public String[] getArgs() {
        return args;
    }

    public enum Argument {

        // Commands
        HELP("help", "h"),
        VERSION("version", "v"),
        DEV("dev"),
        CLI("cli"),
        NO_LOG("nolog"),
        GENERATE("generate", "gen"),

        // Config
        BOT_CONFIG("botconfig"),
        CHANNEL_LIST("channellist"),
        MYSQL_CONFIG("mysqlconfig"),
        HTTPS_SERVER("httpserver"),
        API_CONFIG("apiconfig"),
        OPENAI_CONFIG("openaiconfig"),
        HOST("host"),
        PORT("port");

        private final String name;
        private final HashSet<String> alias;

        Argument(String name, String... alias) {
            this.name = name;
            this.alias = new HashSet<>(Arrays.asList(alias));
        }

        public boolean hasNameOrAlias(String input) {
            return name.equals(input.toLowerCase()) || alias.contains(input.toLowerCase());
        }

        public boolean hasNameOrAlias(ArrayList<String> args) {
            if (args == null || args.isEmpty()) return false;
            for (String arg : args) if (hasNameOrAlias(arg)) return true;
            return false;
        }

        public String getConfig(ArrayList<String> args) {

            // Check
            if (this == HELP || this == VERSION || this == DEV || this == CLI || this == NO_LOG || this == GENERATE) return null;
            if (args == null || args.isEmpty()) return null;
            if (!hasNameOrAlias(args)) return null;

            // Get Config
            int index = args.indexOf("-" + name);
            if (index == -1) index = args.indexOf("/" + name);
            if (index == -1) index = args.indexOf(name);
            if (index == -1) return null;
            return args.get(index + 1);

        }
    }
}