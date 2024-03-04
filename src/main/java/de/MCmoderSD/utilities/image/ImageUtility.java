package de.MCmoderSD.utilities.image;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

@SuppressWarnings("unused")
public abstract class ImageUtility {

    // Attributes
    protected HashMap<String, BufferedImage> bufferedImageCache;
    protected HashMap<String, ImageIcon> imageIconCache;
    protected String url;
    protected boolean isAbsolute;

    // Default Constructor
    public ImageUtility() {
        isAbsolute = false;
        bufferedImageCache = new HashMap<>();
        imageIconCache = new HashMap<>();
        url = null;
    }

    // Constructor with url
    public ImageUtility(String url) {
        isAbsolute = false;
        bufferedImageCache = new HashMap<>();
        imageIconCache = new HashMap<>();
        this.url = url;
    }

    // Constructor with isAbsolute
    public ImageUtility(boolean isAbsolute) {
        this.isAbsolute = isAbsolute;
        bufferedImageCache = new HashMap<>();
        imageIconCache = new HashMap<>();
        url = null;
    }

    // Read image file and return BufferedImage
    public abstract BufferedImage read(String resource);

    public BufferedImage read(String resource, int width, int height) {
        return scaleImage(read(resource), width, height);
    }

    public BufferedImage read(String resource, int scale) {
        return scaleImage(read(resource), scale);
    }

    public BufferedImage read(URL url) {
        if (!url.toString().endsWith(".png") && !url.toString().endsWith(".jpg") && !url.toString().endsWith(".jpeg") && !url.toString().endsWith(".gif"))
            throw new IllegalArgumentException("Unsupported image format: " + url); // Image format is not supported
        if (bufferedImageCache.containsKey(url.toString()))
            return bufferedImageCache.get(url.toString()); // Checks the cache for the image

        BufferedImage image = null;

        try {
            image = ImageIO.read(url);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Null check
        if (image == null) throw new IllegalArgumentException("The image could not be loaded: " + url);

        bufferedImageCache.put(url.toString(), image);
        return image;
    }

    public BufferedImage read(URL url, int width, int height) {
        return scaleImage(read(url), width, height);
    }

    public BufferedImage read(URL url, int scale) {
        return scaleImage(read(url), scale);
    }

    public BufferedImage read(ImageIcon image) {
        return (BufferedImage) image.getImage();
    }

    public BufferedImage read(ImageIcon image, int width, int height) {
        return scaleImage(read(image), width, height);
    }

    public BufferedImage read(ImageIcon image, int scale) {
        return scaleImage(read(image), scale);
    }

    public BufferedImage read(Image image) {
        return (BufferedImage) image;
    }

    public BufferedImage read(Image image, int width, int height) {
        return scaleImage(read(image), width, height);
    }

    public BufferedImage read(Image image, int scale) {
        return scaleImage(read(image), scale);
    }

    // Create ImageIcon
    public ImageIcon createImageIcon(String resource) {
        if (imageIconCache.containsKey(resource)) return imageIconCache.get(resource);

        if (this.url != null) resource = (this.url + resource);

        ImageIcon imageIcon = new ImageIcon(read(resource));
        imageIconCache.put(resource, imageIcon);

        return imageIcon;
    }

    public ImageIcon createImageIcon(String resource, int width, int height) {
        return new ImageIcon(scaleImage(resource, width, height));
    }

    public ImageIcon createImageIcon(String resource, int scale) {
        return new ImageIcon(scaleImage(resource, scale, scale));
    }

    public ImageIcon createImageIcon(BufferedImage image) {
        return new ImageIcon(image);
    }

    public ImageIcon createImageIcon(BufferedImage image, int width, int height) {
        return new ImageIcon(scaleImage(image, width, height));
    }

    public ImageIcon createImageIcon(BufferedImage image, int scale) {
        return new ImageIcon(scaleImage(image, scale));
    }

    public ImageIcon createImageIcon(URL url) {
        return new ImageIcon(read(url));
    }

    public ImageIcon createImageIcon(URL url, int width, int height) {
        return new ImageIcon(scaleImage(url, width, height));
    }

    public ImageIcon createImageIcon(URL url, int scale) {
        return new ImageIcon(scaleImage(url, scale));
    }

    public ImageIcon createImageIcon(Image image) {
        return new ImageIcon(image);
    }

    public ImageIcon createImageIcon(Image image, int width, int height) {
        return new ImageIcon(scaleImage(image, width, height));
    }

    public ImageIcon createImageIcon(Image image, int scale) {
        return new ImageIcon(scaleImage(image, scale));
    }

    // Animation Loader
    public abstract ImageIcon readGif(String resource);

    public ImageIcon readGif(String resource, int width, int height) {
        return new ImageIcon(readGif(resource).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
    }

    public ImageIcon readGif(String resource, int scale) {
        return readGif(resource, scale, scale);
    }

    public ImageIcon readGif(URL url) {
        if (!url.toString().endsWith(".gif"))
            throw new IllegalArgumentException("Unsupported image format: " + url); // Animation format is not supported
        if (imageIconCache.containsKey(url.toString()))
            return imageIconCache.get(url.toString()); // Checks the cache for the Animation


        ImageIcon imageIcon = new ImageIcon(url);

        imageIconCache.put(url.toString(), imageIcon);
        return imageIcon;
    }

    public ImageIcon readGif(URL url, int width, int height) {
        return new ImageIcon(readGif(url).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
    }

    public ImageIcon readGif(URL url, int scale) {
        return readGif(url, scale, scale);
    }


    // Image scaling
    public BufferedImage scaleImage(BufferedImage image, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        scaledImage.getGraphics().drawImage(image, 0, 0, width, height, null);
        return scaledImage;
    }

    public BufferedImage scaleImage(BufferedImage image, int scale) {
        return scaleImage(image, scale, scale);
    }

    public BufferedImage scaleImage(String resource, int width, int height) {
        return scaleImage(read(resource), width, height);
    }

    public BufferedImage scaleImage(String resource, int scale) {
        return scaleImage(resource, scale, scale);
    }

    public BufferedImage scaleImage(URL url, int width, int height) {
        return scaleImage(read(url), width, height);
    }

    public BufferedImage scaleImage(URL url, int scale) {
        return scaleImage(url, scale, scale);
    }

    public BufferedImage scaleImage(ImageIcon image, int width, int height) {
        return scaleImage((BufferedImage) image.getImage(), width, height);
    }

    public BufferedImage scaleImage(ImageIcon image, int scale) {
        return scaleImage(image, scale, scale);
    }

    public BufferedImage scaleImage(Image image, int width, int height) {
        return scaleImage((BufferedImage) image, width, height);
    }

    public BufferedImage scaleImage(Image image, int scale) {
        return scaleImage(image, scale, scale);
    }


    // ImageIcon scaling
    public ImageIcon scaleImageIcon(ImageIcon image, int width, int height) {
        return createImageIcon((BufferedImage) image.getImage(), width, height);
    }

    public ImageIcon scaleImageIcon(ImageIcon image, int scale) {
        return createImageIcon((BufferedImage) image.getImage(), scale);
    }

    public ImageIcon scaleImageIcon(String resource, int width, int height) {
        return createImageIcon(scaleImage(resource, width, height));
    }

    public ImageIcon scaleImageIcon(String resource, int scale) {
        return createImageIcon(scaleImage(resource, scale));
    }

    public ImageIcon scaleImageIcon(URL url, int width, int height) {
        return createImageIcon(scaleImage(url, width, height));
    }

    public ImageIcon scaleImageIcon(URL url, int scale) {
        return createImageIcon(scaleImage(url, scale));
    }

    public ImageIcon scaleImageIcon(BufferedImage image, int width, int height) {
        return createImageIcon(scaleImage(image, width, height));
    }

    public ImageIcon scaleImageIcon(BufferedImage image, int scale) {
        return createImageIcon(scaleImage(image, scale));
    }

    public ImageIcon scaleImageIcon(Image image, int width, int height) {
        return createImageIcon(scaleImage(image, width, height));
    }

    public ImageIcon scaleImageIcon(Image image, int scale) {
        return createImageIcon(scaleImage(image, scale));
    }

    // Animation scaling
    public ImageIcon scaleAnimation(String resource, int width, int height) {
        return new ImageIcon(readGif(resource).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
    }

    public ImageIcon scaleAnimation(String resource, int scale) {
        return scaleAnimation(resource, scale, scale);
    }

    public ImageIcon scaleAnimation(ImageIcon image, int width, int height) {
        return new ImageIcon(image.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
    }

    public ImageIcon scaleAnimation(ImageIcon image, int scale) {
        return scaleAnimation(image, scale, scale);
    }

    public ImageIcon scaleAnimation(URL url, int width, int height) {
        return new ImageIcon(readGif(url).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
    }

    public ImageIcon scaleAnimation(URL url, int scale) {
        return scaleAnimation(url, scale, scale);
    }

    // Setter
    public void addImageToCache(String resource, BufferedImage image) {
        bufferedImageCache.put(resource, image);
    }

    public void addImageToCache(String resource, ImageIcon image) {
        imageIconCache.put(resource, image);
    }

    public void clearImageIconCache() {
        imageIconCache.clear();
    }

    public void removeImageIcon(String resource) {
        imageIconCache.remove(resource);
    }

    public void clearImageCache() {
        bufferedImageCache.clear();
    }

    public void removeImage(String resource) {
        bufferedImageCache.remove(resource);
    }

    public void clearCache() {
        bufferedImageCache.clear();
        imageIconCache.clear();
    }

    public void switchMode() {
        isAbsolute = !isAbsolute;
    }

    // Getter
    public HashMap<String, BufferedImage> getBufferedImageCache() {
        return bufferedImageCache;
    }

    public HashMap<String, ImageIcon> getImageIconCache() {
        return imageIconCache;
    }

    public boolean isCached(String resource, boolean isImageIcon) {
        return isImageIcon ? imageIconCache.containsKey(resource) : bufferedImageCache.containsKey(resource);
    }

    public boolean isCached(BufferedImage image) {
        return bufferedImageCache.containsValue(image);
    }

    public boolean isCached(ImageIcon image) {
        return imageIconCache.containsValue(image);
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public void setAbsolute(boolean isAbsolute) {
        this.isAbsolute = isAbsolute;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }
}