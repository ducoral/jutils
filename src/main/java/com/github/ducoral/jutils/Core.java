package com.github.ducoral.jutils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public final class Core {

    public static final String JSON_TIME_FORMAT = "hh:mm:ss";

    public static final String JSON_DATETIME_FORMAT = "yyyy-MM-dd " + JSON_TIME_FORMAT;

    public static final String PARAM_REGEX_TEMPLATE = "\\$\\{%s}";

    public static final String DOTTED_IDENTIFIER_REGEX = "((\\w+\\.)?\\w+)";

    public static final int DOTTED_IDENTIFIER_GROUP = 1;

    public static final Pattern PARAM_PATTERN = compile(format(PARAM_REGEX_TEMPLATE, DOTTED_IDENTIFIER_REGEX));

    public enum Align { LEFT, CENTER, RIGHT}

    public static Object secondTimeReturns(String value) {
        return new Object() {
            int time = 0;
            public String toString() {
                return time++ == 0 ? "" : value;
            }
        };
    }

    public interface Appendable {
        Appendable append(Object... values);
    }

    public static Appendable appendable(String separator) {
        return new Appendable() {
            final StringBuilder builder = new StringBuilder();
            final Object delimiter = secondTimeReturns(separator);
            public Appendable append(Object... values) {
                for (Object value : values)
                    builder.append(delimiter).append(value);
                return this;
            }
            public String toString() {
                return builder.toString();
            }
        };
    }

    public static List<Object> list(Object... values) {
        return new ArrayList<Object>() {{
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

    public static String format(String format, Object args) {
        return java.lang.String.format(format, args);
    }

    public static String format(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static String lower(Object value) {
        return String.valueOf(value).toLowerCase();
    }

    public static String upper(Object value) {
        return String.valueOf(value).toUpperCase();
    }

    public static boolean isOneOf(String value, String... values) {
        for (String item : values)
            if (Objects.equals(value, item))
                return true;
        return false;
    }

    public static String json(Object value) {
        if (value instanceof List)
            return json((List<?>) value);
        else if (value instanceof Map)
            return json((Map<?,?>) value);
        else if (value instanceof Time)
            return json(format((Time) value, JSON_TIME_FORMAT));
        else if (value instanceof Date)
            return json(format((Date) value, JSON_DATETIME_FORMAT));
        else {
            String str = String.valueOf(value);
            return value instanceof String ? '"' + str + '"' : str;
        }
    };

    private static String json(List<?> list) {
        StringBuilder array = new StringBuilder("[");
        Object comma = secondTimeReturns(",");
        list.forEach(value -> array.append(comma).append(json(value)));
        return array.append("]").toString();
    }

    private static String json(Map<?, ?> map) {
        StringBuilder object = new StringBuilder("{");
        Object comma = secondTimeReturns(",");
        map.forEach((key, value) -> object
                .append(comma)
                .append(json(key))
                .append(':')
                .append(json(value)));
        return object.append("}").toString();
    }

    public static String extract(String template, List<Object> params) {
        Matcher matcher = PARAM_PATTERN.matcher(template);
        while (matcher.find()) {
            String param = matcher.group(DOTTED_IDENTIFIER_GROUP);
            params.add(param);
            template = template.replaceFirst(format(PARAM_REGEX_TEMPLATE, param), "?");
        }
        return template;
    }

    @FunctionalInterface
    public interface Command {
        void execute();
    }

    public static void times(int times, Command command) {
        for (int i = 0; i < times; i++)
            command.execute();
    }

    public static List<Object> values(List<Object> keys, Map<String, Object> map) {
        return new ArrayList<Object>() {{
            for (Object key : keys)
                add(map.get(key));
        }};
    }

    public interface MapBuilder {
        MapBuilder pair(String key, Object value);
        MapBuilder ignoreKeyCase();
        Map<String, Object> done();
    }

    public static MapBuilder map() {
        return new MapBuilder() {
            final Map<String, Object> map = new HashMap<>();
            boolean ignoreKeyCase = false;

            public MapBuilder pair(String key, Object value) {
                map.put(key, value);
                return this;
            }

            public MapBuilder ignoreKeyCase() {
                ignoreKeyCase = true;
                return this;
            }

            public Map<String, Object> done() {
                return ignoreKeyCase ? Core.ignoreKeyCase(map) : map;
            }
        };
    }

    public static Map<String, Object> ignoreKeyCase(Map<String, Object> map) {
        return new IgnoreCaseHashMap(map);
    }

    public static Map<String, Object> map(ResultSet rs) {
        try {
            return new HashMap<String, Object>() {{
                ResultSetMetaData metaData = rs.getMetaData();
                for (int index = 1; index <= metaData.getColumnCount(); index++)
                    put(metaData.getColumnName(index).toLowerCase(), rs.getObject(index));
            }};
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    public static Map<String, Object> merge(Map<String, Object> map1, Map<String, Object> map2) {
        Map<String, Object> merged = new HashMap<>();
        merged.putAll(map1);
        merged.putAll(map2);
        return merged;
    }

    public static Map<String, Object> rename(Map<String, Object> map, Function<String, String> renameFunction) {
        return new HashMap<String, Object>() {{
            for (Entry<String, Object> entry : map.entrySet())
                put(renameFunction.apply(entry.getKey()), entry.getValue());
        }};
    }


    private Core() {
    }
}
