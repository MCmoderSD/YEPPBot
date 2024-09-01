package de.MCmoderSD.utilities.other;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.MCmoderSD.objects.AudioFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import static de.MCmoderSD.utilities.other.Calculate.*;

@SuppressWarnings("HttpUrlsUsage")
public class AudioBroadcast {

    // Constants
    private final String hostname;
    private final int port;

    // Attributes
    private final HttpServer server;
    private final HashMap<String, byte[]> audioFiles;
    private final HashMap<String, AtomicLong> versions;

    // Constructor
    public AudioBroadcast(String hostname, int port) {

        // Set Constants
        this.hostname = hostname;
        this.port = port;

        // Init Attributes
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
        server.createContext("/" + broadcastId, new FrontendHandler(broadcastId));
        server.createContext("/version/" + broadcastId, new VersionHandler(broadcastId));
        server.createContext("/audio/" + broadcastId, new AudioHandler(broadcastId));
        return String.format("Broadcast started on http://%s:%d/%s", hostname, port, broadcastId);
    }

    // Play
    public void play(String broadcastId, AudioFile audioFile) {
        audioFiles.put(broadcastId, audioFile.getAudioData());
        versions.get(broadcastId).incrementAndGet();
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
                           <audio id="audi" autoplay="true" src="http://%s:%d/audio/%s" type="audio/wav">
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
}