package de.MCmoderSD.enums;

public enum ImageFormat {

    // Image Formats
    JPEG, PNG, GIF;

    public static ImageFormat getFormat(String path) {

        // Check Parameters
        if (path == null || path.isBlank()) throw new IllegalArgumentException("Path cannot be null or blank");

        // Determine Format from URL
        return switch (path.substring(path.lastIndexOf('.') + 1).toLowerCase()) {
            case "jpeg", "jpg" -> JPEG;
            case "png" -> PNG;
            case "gif" -> GIF;
            default -> throw new IllegalArgumentException("Unsupported image format: " + path);
        };
    }
}
