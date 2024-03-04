package de.MCmoderSD.utilities.image;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@SuppressWarnings("unused")
public class ImageReader extends ImageUtility {

    // Default Constructor
    public ImageReader() {
        super();
    }

    // Constructor with isAbsolute
    public ImageReader(boolean isAbsolute) {
        super(isAbsolute);
    }

    @Override
    public BufferedImage read(String resource) {
        if (!resource.endsWith(".png") && !resource.endsWith(".jpg") && !resource.endsWith(".jpeg") && !resource.endsWith(".gif"))
            throw new IllegalArgumentException("Unsupported image format: " + resource); // Image format is not supported
        if (bufferedImageCache.containsKey(resource))
            return bufferedImageCache.get(resource); // Checks the cache for the image

        BufferedImage image = null;
        try {
            if (isAbsolute) image = ImageIO.read(Files.newInputStream(Paths.get(resource))); // Image is local
            else
                image = ImageIO.read((Objects.requireNonNull(getClass().getResource(resource)))); // Image is in the JAR file
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Null check
        if (image == null)
            throw new IllegalArgumentException("The image could not be loaded: " + resource); // Image could not be loaded (Image is null)

        // Add to cache
        bufferedImageCache.put(resource, image);
        return image;
    }

    @Override
    public ImageIcon readGif(String resource) {
        if (!resource.endsWith(".gif"))
            throw new IllegalArgumentException("Unsupported image format: " + resource); // Animation format is not supported

        if (imageIconCache.containsKey(resource))
            return imageIconCache.get(resource); // Checks the cache for the Animation

        if (resource.startsWith("/")) resource = resource.substring(1); // Remove the first slash

        ImageIcon imageIcon = null;
        try {
            URL url;
            if (isAbsolute) url = Paths.get(resource).toUri().toURL(); // Animation is local
            else url = getClass().getClassLoader().getResource(resource); // Image is in the JAR file
            imageIcon = new ImageIcon(Objects.requireNonNull(url)); // Load the Animation
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
        }

        // Null check
        if (imageIcon == null) throw new IllegalArgumentException("The image could not be loaded: " + resource);

        imageIconCache.put(resource, imageIcon);
        return imageIcon;
    }
}