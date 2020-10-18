package com.github.ducoral.jutils;

import java.util.*;

public final class Core {
    public enum Align { LEFT, CENTER, RIGHT}

    public static class JsonMap extends HashMap<String, Object> { }

    @SafeVarargs
    public static <T> List<T> list(T... values) {
        return new ArrayList<T>() {{
            addAll(Arrays.asList(values));
        }};
    }

    public static String str(int length, char fill) {
        return new String(new byte[length]).replace('\0', fill);
    }

    public static String safe(String value) {
        return value == null ? "" : value;
    }

    public static String fix(String value, int width) {
        return fix(value, width, Align.LEFT, ' ');
    }

    public static String fix(String value, int width, Align align, char fill) {
        int diff = width - value.length();
        if (diff < 1)
            return value;
        switch (align) {
            case LEFT: return value + str(diff, fill);
            case RIGHT: return str(diff, fill) + value;
            default:
                int half = diff / 2;
                return str(half, fill) + value + str(diff - half, fill);
        }
    }

    public static String json(Object value) {
        if (value instanceof List)
            return json((List<?>) value);
        else if (value instanceof Map)
            return json((Map<?,?>) value);
        else {
            String str = String.valueOf(value);
            return value instanceof String ? '"' + str + '"' : str;
        }
    };

    private static String json(List<?> list) {
        StringBuilder array = new StringBuilder("[");
        String comma = "";
        for (Object value : list) {
            array.append(comma).append(json(value));
            comma = ",";
        }
        return array.append("]").toString();
    }

    private static String json(Map<?, ?> map) {
        StringBuilder object = new StringBuilder("{");
        String comma = "";
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            object.append(comma).append(json(entry.getKey())).append(':').append(json(entry.getValue()));
            comma = ",";
        }
        return object.append("}").toString();
    }

    private Core() {
    }
}
