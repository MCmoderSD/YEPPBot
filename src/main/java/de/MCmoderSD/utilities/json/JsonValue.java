package de.MCmoderSD.utilities.json;

import java.awt.Color;

@SuppressWarnings("unused")
public class JsonValue {

    // Attribute
    private final String value;

    // Constructor
    public JsonValue(String value) {
        this.value = value;
    }

    // Getters
    public String asText() {
        return value;
    }

    public byte asByte() {
        return Byte.parseByte(value);
    }

    public short asShort() {
        return Short.parseShort(value);
    }

    public int asInt() {
        return Integer.parseInt(value);
    }

    public long asLong() {
        return Long.parseLong(value);
    }

    public float asFloat() {
        return Float.parseFloat(value);
    }

    public double asDouble() {
        return Double.parseDouble(value);
    }

    public boolean asBoolean() {
        return Boolean.parseBoolean(value);
    }

    public char[] asChar() {
        return value.toCharArray();
    }

    public char asChar(int index) {
        return value.charAt(index);
    }

    public JsonValue asValue() {
        return this;
    }

    public Color asColor() {
        Color color = null;

        if (value.startsWith("#")) color = new Color(Integer.parseInt(value.substring(1), 16));
        if (value.startsWith("java.awt.Color"))
            color = new Color(Integer.parseInt(value.split("=")[1].split(",")[0]), Integer.parseInt(value.split("=")[2].split(",")[0]), Integer.parseInt(value.split("=")[3].split("]")[0]));

        return color;
    }
}