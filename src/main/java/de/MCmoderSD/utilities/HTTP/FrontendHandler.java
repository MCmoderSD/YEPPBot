package de.MCmoderSD.utilities.HTTP;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

@SuppressWarnings("HttpUrlsUsage")
public class FrontendHandler implements HttpHandler {

    // Constants
    private final String hostname;
    private final int port;

    // Attributes
    private final String broadcastId;

    // Constructor
    public FrontendHandler(String broadcastId, String hostname, int port) {

        // Set Attributes
        this.broadcastId = broadcastId;

        // Set Constants
        this.hostname = hostname;
        this.port = port;
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
