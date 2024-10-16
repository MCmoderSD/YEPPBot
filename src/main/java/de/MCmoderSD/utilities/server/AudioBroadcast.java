package de.MCmoderSD.utilities.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.MCmoderSD.objects.AudioFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class AudioBroadcast {

    // Constants
    private final String hostname;
    private final int port;

    // Attributes
    private final Server server;
    private final HashMap<String, HashSet<HttpContext>> serverContexts;
    private final HashMap<String, byte[]> audioFiles;
    private final HashMap<String, AtomicLong> versions;

    // Constructor
    public AudioBroadcast(Server server) {

        // Set Constants
        this.server = server;

        // Set Attributes
        hostname = server.getHostname();
        port = server.getPort();

        // Init Attributes
        serverContexts = new HashMap<>();
        audioFiles = new HashMap<>();
        versions = new HashMap<>();
    }

    // Register Broadcast
    public String registerBroadcast(String broadcastId) {

        // Create Contexts
        HashSet<HttpContext> contexts = new HashSet<>();

        // Add Contexts
        contexts.add(server.getHttpsServer().createContext("/" + broadcastId, new FrontendHandler(broadcastId)));
        contexts.add(server.getHttpsServer().createContext("/audio/" + broadcastId, new AudioHandler(broadcastId)));
        contexts.add(server.getHttpsServer().createContext("/version/" + broadcastId, new VersionHandler(broadcastId)));

        // Add Contexts to Server
        serverContexts.put(broadcastId, contexts);

        // Return
        return String.format("Broadcast started on https://%s:%d/%s", hostname, port, broadcastId);
    }

    // Unregister Broadcast
    public boolean unregisterBroadcast(String broadcastId) {
        if (!serverContexts.containsKey(broadcastId)) return false;
        serverContexts.get(broadcastId).forEach(server.getHttpsServer()::removeContext);
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

    // Frontend Handler
    private class FrontendHandler implements HttpHandler {

        // Attributes
        private final String broadcastId;

        // Constructor
        public FrontendHandler(String broadcastId) {
            this.broadcastId = broadcastId;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = String.format("""
                    <html>
                        <body style='margin:0; padding:0; overflow:hidden;'>
                           <audio id="audi" autoplay="true" src="https://%s:%d/audio/%s" type="audio/wav">
                            <script>
                                function checkForUpdate() {
                                    fetch('/version/%s')
                                        .then(response => response.text())
                                        .then(version => {
                                            if (localStorage.getItem('audioVersion') !== version) {
                                                localStorage.setItem('audioVersion', version);
                                                window.location.reload();
                                            }
                                        });
                                }
                                function updateLoop() {
                                    setInterval(checkForUpdate, 1000);
                                }

                                window.onload = updateLoop;
                            </script>
                        </body>
                    </html>
                    """, hostname, port, broadcastId, broadcastId);

            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            OutputStream os = exchange.getResponseBody();
            os.write(html.getBytes());
            os.close();
        }
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

    public Server getServer() {
        return server;
    }

    public HashMap<String, byte[]> getAudioFiles() {
        return audioFiles;
    }

    public HashMap<String, AtomicLong> getVersions() {
        return versions;
    }
}