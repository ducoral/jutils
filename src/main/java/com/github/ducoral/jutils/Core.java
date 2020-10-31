package com.github.ducoral.jutils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Core {

    public static final String JSON_TIME_FORMAT = "hh:mm:ss";

    public static final String JSON_DATETIME_FORMAT = "yyyy-MM-ddT" + JSON_TIME_FORMAT;

    public static final Pattern PARAM_PATTERN = Pattern.compile("\\$\\{\\w+\\.?\\w*}");

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

    public static String replace(String str, String target, String replacement) {
        int index = str.indexOf(target);
        if (index > -1)
            str = str.substring(0, index) + replacement + str.substring(index + target.length());
        return str;
    }

    public static String format(String template, Pair... parameters) {
        return format(template, map(parameters));
    }

    public static String format(String template, Map<String, Object> scope) {
        List<String> params = new ArrayList<>();
        List<Object> replacements = new ArrayList<>();
        template = extract(template, params, "%s");
        for (int index = 0; index < params.size(); index++)
            replacements.add(scope.get(params.get(index)));
        return format(template, replacements.toArray());
    }

    public static String format(String format, Object... args) {
        return String.format(format, args);
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

    public static String extract(String template, List<String> params, String replacement) {
        Matcher matcher = PARAM_PATTERN.matcher(template);
        while (matcher.find()) {
            String param = matcher.group(0);
            params.add(param.substring(2, param.length() - 1));
            template = replace(template, param, replacement);
        }
        return template;
    }

    public static byte[] bytes(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1)
                baos.write(buffer,0, len);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static InputStream stream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    public static PropertyResourceBundle properties(String name) {
        ClassLoader loader = Core.class.getClassLoader();
        URL resource = loader.getResource(format("%s_%s.properties", name, Locale.getDefault()));
        if (resource == null)
            resource = loader.getResource(format("%s.properties", name));
        if (resource == null)
            throw new Oops("Could not load property file");
        try {
            return new PropertyResourceBundle(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    public interface Command {
        void execute();
    }

    public static void times(int times, Command command) {
        for (int i = 0; i < times; i++)
            command.execute();
    }

    public static List<Object> values(List<String> keys, Map<String, Object> map) {
        return new ArrayList<Object>() {{
            for (String key : keys)
                add(map.get(key));
        }};
    }

    public interface Pair {
        String key();
        Object value();
    }

    public static Pair pair(String key, Object value) {
        return new Pair() {
            public String key() {
                return key;
            }
            public Object value() {
                return value;
            }
        };
    }

    public static Map<String, Object> map(Pair... pairs) {
        return new HashMap<String, Object>() {{
            for (Pair pair : pairs)
                put(pair.key(), pair.value());
        }};
    }

    public interface MapBuilder {
        MapBuilder pair(String key, Object value);
        MapBuilder merge(Map<String, Object> map);
        MapBuilder rename(Function<String, String> renameKeyFunction);
        MapBuilder ignore();
        Map<String, Object> done();
    }

    public static MapBuilder map() {
        return map(new HashMap<>());
    }

    public static MapBuilder map(Map<String, Object> source) {
        return new MapBuilder() {
            Map<String, Object> map = new HashMap<>(source);
            boolean ignore = false;

            public MapBuilder merge(Map<String, Object> map) {
                this.map.putAll(map);
                return this;
            }

            public MapBuilder rename(Function<String, String> renameKeyFunction) {
                map = new HashMap<String, Object>() {{
                    for (String key : map.keySet())
                        put(renameKeyFunction.apply(key), map.get(key));
                }};
                return this;
            }

            public MapBuilder pair(String key, Object value) {
                map.put(key, value);
                return this;
            }

            public MapBuilder ignore() {
                ignore = true;
                return this;
            }

            public Map<String, Object> done() {
                return ignore ? ignoreKeyCase(map) : map;
            }
        };
    }

    public static MapBuilder map(ResultSet rs) {
        try {
            HashMap<String, Object> resultSetMap = new HashMap<String, Object>() {{
                ResultSetMetaData metaData = rs.getMetaData();
                for (int index = 1; index <= metaData.getColumnCount(); index++)
                    put(metaData.getColumnName(index), rs.getObject(index));
            }};
            return map(resultSetMap);
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    public static Map<String, Object> ignoreKeyCase(Map<String, Object> map) {
        return new IgnoreCaseHashMap(map);
    }

    private Core() {
    }
}
