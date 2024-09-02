package de.MCmoderSD.utilities.HTTP;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.MCmoderSD.objects.AudioFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

import static de.MCmoderSD.utilities.other.Calculate.*;

@SuppressWarnings({"HttpUrlsUsage", "unused", "UnusedReturnValue"})
public class AudioBroadcast {

    // Constants
    private final String hostname;
    private final int port;

    // Attributes
    private final HttpServer server;
    private final HashMap<String, HashSet<HttpContext>> serverContexts;
    private final HashMap<String, byte[]> audioFiles;
    private final HashMap<String, AtomicLong> versions;

    // Constructor
    public AudioBroadcast(String hostname, int port) {

        // Set Constants
        this.hostname = hostname;
        this.port = port;

        // Init Attributes
        serverContexts = new HashMap<>();
        audioFiles = new HashMap<>();
        versions = new HashMap<>();

        // Start Server
        try {
            server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.printf("Server started on http://%s:%d%s", hostname, port, BREAK);
    }

    // Register Broadcast
    public String registerBrodcast(String broadcastId) {

        // Create Contexts
        HashSet<HttpContext> contexts = new HashSet<>();

        // Add Contexts
        contexts.add(server.createContext("/" + broadcastId, new FrontendHandler(broadcastId, hostname, port)));
        contexts.add(server.createContext("/audio/" + broadcastId, new AudioHandler(broadcastId)));
        contexts.add(server.createContext("/version/" + broadcastId, new VersionHandler(broadcastId)));

        // Add Contexts to Server
        serverContexts.put(broadcastId, contexts);

        // Return
        return String.format("Broadcast started on http://%s:%d/%s", hostname, port, broadcastId);
    }

    // Unregister Broadcast
    public boolean unregisterBroadcast(String broadcastId) {
        if (!serverContexts.containsKey(broadcastId)) return false;
        serverContexts.get(broadcastId).forEach(server::removeContext);
        serverContexts.remove(broadcastId);
        audioFiles.remove(broadcastId);
        versions.remove(broadcastId);
        return true;
    }

    // Play
    public boolean play(String broadcastId, AudioFile audioFile) {
        if (!serverContexts.containsKey(broadcastId)) return false;
        audioFiles.put(broadcastId, audioFile.getAudioData());
        versions.get(broadcastId).incrementAndGet();
        return true;
    }

    // Audio Handler
    private class AudioHandler implements HttpHandler {

        // Attributes
        private final String broadcastId;

        // Constructor
        public AudioHandler(String broadcastId) {
            this.broadcastId = broadcastId;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "audio/wav");

            if (audioFiles.containsKey(broadcastId) && audioFiles.get(broadcastId) != null) {
                byte[] audioData = audioFiles.get(broadcastId);
                exchange.sendResponseHeaders(200, audioData.length);
                OutputStream os = exchange.getResponseBody();
                os.write(audioData);
                os.close();
            } else exchange.sendResponseHeaders(404, -1);
        }
    }

    // Version Handler
    private class VersionHandler implements HttpHandler {

        // Attributes
        private final String broadcastId;

        // Constructor
        public VersionHandler(String broadcastId) {
            this.broadcastId = broadcastId;
            versions.put(broadcastId, new AtomicLong(0));
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String currentVersion = String.valueOf(versions.get(broadcastId).get());
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, currentVersion.length());
            OutputStream os = exchange.getResponseBody();
            os.write(currentVersion.getBytes());
            os.close();
        }
    }

    // Getter
    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public HttpServer getServer() {
        return server;
    }

    public HashMap<String, byte[]> getAudioFiles() {
        return audioFiles;
    }

    public HashMap<String, AtomicLong> getVersions() {
        return versions;
    }
}