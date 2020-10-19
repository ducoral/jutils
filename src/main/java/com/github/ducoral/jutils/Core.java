package com.github.ducoral.jutils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Core {

    public static final Pattern PARAM_PATTERN = Pattern.compile(paramRegex("((\\w+\\.)?\\w+)"));

    public enum Align { LEFT, CENTER, RIGHT}

    public static class JsonMap extends HashMap<String, Object> { }

    public static class Comma {
        int time = 0;
        @Override
        public String toString() {
            return time++ == 0 ? "" : ",";
        }
    }

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
        final Comma comma = new Comma();
        list.forEach(value -> array.append(comma).append(json(value)));
        return array.append("]").toString();
    }

    private static String json(Map<?, ?> map) {
        StringBuilder object = new StringBuilder("{");
        final Comma comma = new Comma();
        map.forEach((key, value) -> object
                .append(comma)
                .append(json(key))
                .append(':')
                .append(json(value)));
        return object.append("}").toString();
    }

    public static String params(String string, List<String> params) {
        Matcher matcher = PARAM_PATTERN.matcher(string);
        while (matcher.find()) {
            String param = matcher.group(1);
            params.add(param);
            string = string.replaceFirst(paramRegex(param), "?");
        }
        return string;
    }

    private static String paramRegex(String fieldRegex) {
        return String.format("\\$\\{%s}", fieldRegex);
    }

    private Core() {
    }
}
