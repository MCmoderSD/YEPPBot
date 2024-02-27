package de.MCmoderSD.utilities.json;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings("unused")
public class JsonNode {

    // Attributes
    private final String path;
    private final String url;
    private final boolean isAbsolute;
    private final HashMap<String, JsonValue> jsonMap;

    // Default constructor
    public JsonNode(String path) {
        this.url = null;
        this.path = path;
        this.isAbsolute = false;
        this.jsonMap = new HashMap<>();

        readResource(path);
    }

    // Constructor with isAbsolute path
    public JsonNode(String path, boolean isAbsolute) {
        this.url = null;
        this.path = path;
        this.isAbsolute = isAbsolute;
        this.jsonMap = new HashMap<>();

        if (isAbsolute) readAbsolute(path);
        else readResource(path);
    }

    // Constructor with url and path
    public JsonNode(String url, String path) {
        this.url = url;
        this.path = path;
        this.isAbsolute = false;
        this.jsonMap = new HashMap<>();

        readUrl(url, path);
    }

    // Copy constructor
    public JsonNode(JsonNode jsonNode) {
        this.url = jsonNode.getUrl();
        this.path = jsonNode.getPath();
        this.isAbsolute = jsonNode.isAbsolute();
        this.jsonMap = jsonNode.getJsonMap();
    }

    // Constructor with jsonMap
    public JsonNode(HashMap<String, JsonValue> jsonMap) {
        this.url = null;
        this.path = null;
        this.isAbsolute = false;
        this.jsonMap = jsonMap;
    }

    // Read json from resource path
    private void readResource(String path) {
        try {
            decodeJson(new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(path)))));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // Read json from absolute path
    private void readAbsolute(String path) {
        try {
            decodeJson(new BufferedReader(new FileReader(path)));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // Read json from url and path
    private void readUrl(String url, String path) {
        try {
            decodeJson(new BufferedReader(new InputStreamReader(new URL(url + path).openStream())));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    // Decode json
    private void decodeJson(BufferedReader bufferedReader) throws IOException {
        StringBuilder jsonString = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) jsonString.append(line);

        String content = jsonString.toString().trim();
        content = content.substring(1, content.length() - 1);
        String[] keyValuePairs = content.split(",\\s*");

        for (String pair : keyValuePairs) {
            int colonIndex = pair.indexOf(":");
            if (colonIndex != -1) {
                String key = pair.substring(0, colonIndex).trim().replaceAll("\"", "");
                String rawValue = pair.substring(colonIndex + 1).trim();
                JsonValue value;
                if (rawValue.startsWith("\"") && rawValue.endsWith("\""))
                    value = new JsonValue(rawValue.replaceAll("\"", ""));
                else value = new JsonValue(rawValue);
                jsonMap.put(key, value);
            }
        }
    }

    // Attribute getter
    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }

    public HashMap<String, JsonValue> getJsonMap() {
        return jsonMap;
    }

    public int getSize() {
        return jsonMap.size();
    }

    // Getter with key
    public JsonValue get(String key) {
        return jsonMap.get(key);
    }

    public String getAsText(String key) {
        return jsonMap.get(key).asText();
    }

    public String getAsString(String key) {
        return jsonMap.get(key).asText();
    }

    public byte getAsByte(String key) {
        return jsonMap.get(key).asByte();
    }

    public short getAsShort(String key) {
        return jsonMap.get(key).asShort();
    }

    public int getAsInt(String key) {
        return jsonMap.get(key).asInt();
    }

    public long getAsLong(String key) {
        return jsonMap.get(key).asLong();
    }

    public float getAsFloat(String key) {
        return jsonMap.get(key).asFloat();
    }

    public double getAsDouble(String key) {
        return jsonMap.get(key).asDouble();
    }

    public boolean getAsBoolean(String key) {
        return jsonMap.get(key).asBoolean();
    }

    public char[] getAsChar(String key) {
        return jsonMap.get(key).asChar();
    }

    public char getAsChar(String key, int index) {
        return jsonMap.get(key).asChar(index);
    }

    public JsonValue getAsValue(String key) {
        return jsonMap.get(key).asValue();
    }

    public Color getAsColor(String key) {
        return jsonMap.get(key).asColor();
    }

    // Setter
    public void set(String key, JsonValue value) {
        jsonMap.put(key, value);
    }

    public void set(String[] keys, JsonValue[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], values[i]);
    }

    public void set(String key, String value) {
        jsonMap.put(key, new JsonValue(value));
    }

    public void set(String[] keys, String[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(values[i]));
    }

    public void set(String key, byte value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, byte[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, short value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, short[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, int value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, int[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, long value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, long[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, float value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, float[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, double value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, double[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, boolean value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, boolean[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, char[] value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, char[][] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, char value, int index) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, char[] values, int[] indexes) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[indexes[i]])));
    }

    public void set(String key, JsonNode value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, JsonNode[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, Color value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, Color[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, HashMap<String, JsonValue> value) {
        jsonMap.put(key, new JsonValue(String.valueOf(value)));
    }

    public void set(String[] keys, HashMap<String, JsonValue>[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i])));
    }

    public void set(String key, HashMap<String, JsonValue> value, String valueKey) {
        jsonMap.put(key, new JsonValue(String.valueOf(value.get(valueKey))));
    }

    public void set(String[] keys, HashMap<String, JsonValue>[] values, String[] valueKeys) {
        for (int i = 0; i < keys.length; i++)
            jsonMap.put(keys[i], new JsonValue(String.valueOf(values[i].get(valueKeys[i]))));
    }

    // Replace
    public void replace(HashMap<String, JsonValue> jsonMap) {
        this.jsonMap.clear();
        this.jsonMap.putAll(jsonMap);
    }

    public void replace(String[] keys, JsonValue[] values) {
        for (int i = 0; i < keys.length; i++) jsonMap.replace(keys[i], values[i]);
    }

    public void replace(String key, JsonValue value) {
        jsonMap.replace(key, value);
    }

    public void replace(String key, String value) {
        jsonMap.replace(key, new JsonValue(value));
    }

    public void replace(String key, byte value) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, short value) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, int value) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, long value) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, float value) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, double value) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, boolean value) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, char[] value) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, char value, int index) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, JsonNode value) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, HashMap<String, JsonValue> value) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value)));
    }

    public void replace(String key, HashMap<String, JsonValue> value, String valueKey) {
        jsonMap.replace(key, new JsonValue(String.valueOf(value.get(valueKey))));
    }

    // Add
    public void add(HashMap<String, JsonValue> jsonMap) {
        this.jsonMap.putAll(jsonMap);
    }

    public void add(HashMap<String, JsonValue>[] jsonMaps) {
        for (HashMap<String, JsonValue> jsonMap : jsonMaps) this.jsonMap.putAll(jsonMap);
    }

    // Delete
    public void remove(String key) {
        jsonMap.remove(key);
    }

    public void remove(String[] keys) {
        for (String key : keys) jsonMap.remove(key);
    }

    public void clear() {
        jsonMap.clear();
    }

    // Contains
    public boolean containsKey(String key) {
        return jsonMap.containsKey(key);
    }

    public boolean containsValue(JsonValue value) {
        return jsonMap.containsValue(value);
    }
}