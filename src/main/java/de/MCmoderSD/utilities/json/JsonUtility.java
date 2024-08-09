package de.MCmoderSD.utilities.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@SuppressWarnings("unused")
public class JsonUtility {

    // Attributes
    private final HashMap<String, JsonNode> jsonCache;

    // Default constructor
    public JsonUtility() {

        // Initialize JSON cache
        this.jsonCache = new HashMap<>();
    }

    // Methods
    public JsonNode load(String path) {
        if (jsonCache.containsKey(path)) return jsonCache.get(path);
        try {

            // Load JSON from resources folder in JAR
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;

            // Load JSON from resources folder in JAR
            InputStream inputStream = getClass().getResourceAsStream(path);
            jsonNode = objectMapper.readTree(inputStream);

            // Cache JSON
            jsonCache.put(path, jsonNode);
            return jsonNode; // Return JSON
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonNode load(String path, boolean isAbsolute) {
        if (jsonCache.containsKey(path)) return jsonCache.get(path);
        if (!isAbsolute) return load(path);
        else {
            try {

                // Load JSON from absolute path
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode;

                // Load JSON from absolute path
                File file = new File(path);
                jsonNode = objectMapper.readTree(file);

                // Cache JSON
                jsonCache.put(path, jsonNode);
                return jsonNode; // Return JSON
            } catch (IOException e) {
                throw new RuntimeException("Error loading JSON from absolute path: " + e.getMessage());
            }
        }
    }

    // Clear
    public void clear() {
        jsonCache.clear();
    }
}