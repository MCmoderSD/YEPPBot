package de.MCmoderSD.utilities.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

@SuppressWarnings("unused")
public class JsonUtility {

    // Attributes
    private final HashMap<String, JsonNode> jsonCache;

    private String url;
    private boolean isAbsolute;

    // Default constructor
    public JsonUtility() {
        url = null;
        isAbsolute = false;
        this.jsonCache = new HashMap<>();
    }

    // Constructor with url
    public JsonUtility(String url) {
        this.url = url;
        this.isAbsolute = false;
        this.jsonCache = new HashMap<>();
    }

    // Constructor with isAbsolute path
    public JsonUtility(boolean isAbsolute) {
        this.url = null;
        this.isAbsolute = isAbsolute;
        this.jsonCache = new HashMap<>();
    }

    public JsonUtility(HashMap<String, JsonNode> jsonCache) {
        this.url = null;
        this.isAbsolute = false;
        this.jsonCache = jsonCache;
    }

    public JsonUtility(JsonUtility jsonUtility) {
        this.url = jsonUtility.getURL();
        this.isAbsolute = jsonUtility.isAbsolute();
        this.jsonCache = jsonUtility.getJsonCache();
    }

    // Methods
    public JsonNode load(String path) {
        if (jsonCache.containsKey(path)) return jsonCache.get(path);
        else {
            ObjectMapper mapper = new ObjectMapper();
            try {
                if (isAbsolute) return load(path, true);
                InputStream inputStream = getClass().getResourceAsStream(path);
                if (inputStream == null) return null;
                JsonNode jsonNode = mapper.readTree(inputStream);
                jsonCache.put(path, jsonNode);
                return jsonNode;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public JsonNode[] load(String[] paths) {
        JsonNode[] jsonNodes = new JsonNode[paths.length];
        for (var i = 0; i < paths.length; i++) {
            if (jsonCache.containsKey(paths[i])) jsonNodes[i] = jsonCache.get(paths[i]);
            else {
                JsonNode jsonNode = load(paths[i]);
                jsonCache.put(paths[i], jsonNode);
                jsonNodes[i] = jsonNode;
            }
        }
        return jsonNodes;
    }

    public JsonNode load(String url, String path) {
        if (jsonCache.containsKey(path)) return jsonCache.get(path);
        else {
            ObjectMapper mapper = new ObjectMapper();
            try {
                InputStream inputStream = new URI(url + path).toURL().openStream();
                if (inputStream == null) return null;
                JsonNode jsonNode = mapper.readTree(inputStream);
                jsonCache.put(path, jsonNode);
                return jsonNode;
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public JsonNode[] load(String url, String[] paths) {
        JsonNode[] jsonNodes = new JsonNode[paths.length];
        for (var i = 0; i < paths.length; i++) {
            if (jsonCache.containsKey(paths[i])) jsonNodes[i] = jsonCache.get(paths[i]);
            else {
                JsonNode jsonNode = load(url, paths[i]);
                jsonCache.put(paths[i], jsonNode);
                jsonNodes[i] = jsonNode;
            }
        }
        return jsonNodes;
    }

    public JsonNode[] load(String[] urls, String[] paths) {
        JsonNode[] jsonNodes = new JsonNode[paths.length];
        for (var i = 0; i < paths.length; i++) {
            if (jsonCache.containsKey(paths[i])) jsonNodes[i] = jsonCache.get(paths[i]);
            else {
                JsonNode jsonNode = load(urls[i], paths[i]);
                jsonCache.put(paths[i], jsonNode);
                jsonNodes[i] = jsonNode;
            }
        }
        return jsonNodes;
    }

    public JsonNode load(String path, boolean isAbsolute) {
        if (jsonCache.containsKey(path)) return jsonCache.get(path);
        else {
            ObjectMapper mapper = new ObjectMapper();
            try {
            InputStream inputStream = Files.newInputStream(Paths.get(path));
            JsonNode jsonNode = mapper.readTree(inputStream);
            jsonCache.put(path, jsonNode);
            return jsonNode;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public JsonNode[] load(String[] paths, boolean isAbsolute) {
        JsonNode[] jsonNodes = new JsonNode[paths.length];
        for (var i = 0; i < paths.length; i++) {
            if (jsonCache.containsKey(paths[i])) jsonNodes[i] = jsonCache.get(paths[i]);
            else {
                JsonNode jsonNode = load(paths[i], true);
                jsonCache.put(paths[i], jsonNode);
                jsonNodes[i] = jsonNode;
            }
        }
        return jsonNodes;
    }

    // Getter
    public String getURL() {
        return url;
    }

    // Setter
    public void setURL(String url) {
        this.url = url;
    }

    public boolean isEmpty() {
        return jsonCache.isEmpty();
    }

    public int size() {
        return jsonCache.size();
    }

    public HashMap<String, JsonNode> getJsonCache() {
        return jsonCache;
    }

    public void setJsonCache(HashMap<String, JsonNode> jsonCache) {
        this.jsonCache.clear();
        this.jsonCache.putAll(jsonCache);
    }

    public void setJsonCache(HashMap<String, JsonNode>[] jsonCaches) {
        this.jsonCache.clear();
        for (HashMap<String, JsonNode> jsonCache : jsonCaches) this.jsonCache.putAll(jsonCache);
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public void setAbsolute(boolean isAbsolute) {
        this.isAbsolute = isAbsolute;
    }

    // Remove
    public void remove(String path) {
        jsonCache.remove(path);
    }

    public void remove(String[] json) {
        for (String path : json) jsonCache.remove(path);
    }

    public void add(HashMap<String, JsonNode> jsonCache) {
        this.jsonCache.putAll(jsonCache);
    }

    public void add(HashMap<String, JsonNode>[] jsonCaches) {
        for (HashMap<String, JsonNode> jsonCache : jsonCaches) this.jsonCache.putAll(jsonCache);
    }

    // Replace
    public void replace(String path, JsonNode jsonNode) {
        jsonCache.replace(path, jsonNode);
    }

    public void replace(String path, JsonNode[] jsonNodes) {
        for (JsonNode jsonNode : jsonNodes) jsonCache.replace(path, jsonNode);
    }

    // Clear
    public void clear() {
        jsonCache.clear();
    }
}