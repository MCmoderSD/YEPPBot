package de.MCmoderSD.utilities.image;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("unused")
public class ImageStreamer extends ImageUtility {

    // Default Constructor
    public ImageStreamer() {
        super();
    }

    // Constructor with url
    public ImageStreamer(String url) {
        super(url);
    }

    @Override
    public BufferedImage read(String resource) {
        if (!resource.endsWith(".png") && !resource.endsWith(".jpg") && !resource.endsWith(".jpeg") && !resource.endsWith(".gif"))
            throw new IllegalArgumentException("Unsupported image format: " + resource); // Image format is not supported
        if (bufferedImageCache.containsKey(resource))
            return bufferedImageCache.get(resource); // Checks the cache for the image

        BufferedImage image = null;

        try {
            if (this.url != null && !resource.contains(this.url)) image = ImageIO.read(new URI(this.url + resource).toURL());
            else image = ImageIO.read(new URI(resource).toURL());
        } catch (URISyntaxException | IOException e) {
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

        ImageIcon imageIcon; // Load the Animation

        try {
            if (this.url != null && !resource.contains(this.url)) imageIcon = new ImageIcon(new URI(this.url + resource).toURL());
            else imageIcon = new ImageIcon(new URI(resource).toURL());
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }

        imageIconCache.put(resource, imageIcon);
        return imageIcon;
    }
}