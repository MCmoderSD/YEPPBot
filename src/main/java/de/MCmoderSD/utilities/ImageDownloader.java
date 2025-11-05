package de.MCmoderSD.utilities;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

public class ImageDownloader {

    public static byte[] downloadImage(String url) {

        // Check Parameters
        if (url == null || url.isBlank()) throw new IllegalArgumentException("URL cannot be null or empty");
        if (!(url.startsWith("http://") || url.startsWith("https://"))) throw new IllegalArgumentException("URL must start with http:// or https://");

        // Download Image
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new URI(url).toURL().openStream())) {
            return bufferedInputStream.readAllBytes();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to download image from URL: " + url, e);
        }
    }
}
