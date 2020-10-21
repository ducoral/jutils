package com.github.ducoral.jutils;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;

public final class Core {

    public static final String PARAM_REGEX_TEMPLATE = "\\$\\{%s}";

    public static final String DOTED_IDENTIFIER_REGEX = "((\\w+\\.)?\\w+)";

    public static final int DOTED_IDENTIFIER_GROUP = 1;

    public static final Pattern PARAM_PATTERN = compile(format(PARAM_REGEX_TEMPLATE, DOTED_IDENTIFIER_REGEX));

    public enum Align { LEFT, CENTER, RIGHT}

    public static class Scope extends HashMap<String, Object> { }

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

    public static String extractParams(String template, List<String> params) {
        Matcher matcher = PARAM_PATTERN.matcher(template);
        while (matcher.find()) {
            String param = matcher.group(DOTED_IDENTIFIER_GROUP);
            params.add(param);
            template = template.replaceFirst(format(PARAM_REGEX_TEMPLATE, param), "?");
        }
        return template;
    }

    public static PreparedStatement prepare(PreparedStatement st, List<String> params, Map<String, Object> scope) {
        try {
            for (int index = 0; index < params.size(); index++) {
                Object value = scope.get(params.get(index));
                if (value == null)
                    st.setNull(index, Types.NULL);
                else if (value instanceof Byte)
                    st.setByte(index, (Byte) value);
                else if (value instanceof Short)
                    st.setShort(index, (Short) value);
                else if (value instanceof Integer)
                    st.setInt(index, (Integer) value);
                else if (value instanceof Long)
                    st.setLong(index, (Long) value);
                else if (value instanceof Float)
                    st.setFloat(index, (Float) value);
                else if (value instanceof Double)
                    st.setDouble(index, (Double) value);
                else if (value instanceof BigDecimal)
                    st.setBigDecimal(index, (BigDecimal) value);
                else if (value instanceof String)
                    st.setString(index, (String) value);
                else if (value instanceof java.sql.Date)
                    st.setDate(index, (java.sql.Date) value);
                else if (value instanceof java.sql.Time)
                    st.setTime(index, (java.sql.Time) value);
                else if (value instanceof java.sql.Timestamp)
                    st.setTimestamp(index, (java.sql.Timestamp) value);
                else if (value instanceof Boolean)
                    st.setBoolean(index, (Boolean) value);
                else if (value instanceof byte[])
                    st.setBytes(index, (byte[]) value);
                else if (value instanceof Ref)
                    st.setRef(index, ((Ref) value));
                else if (value instanceof NClob)
                    st.setNClob(index, (NClob) value);
                else if (value instanceof Clob)
                    st.setClob(index, (Clob) value);
                else if (value instanceof Blob)
                    st.setBlob(index, (Blob) value);
                else if (value instanceof Array)
                    st.setArray(index, (Array) value);
                else if (value instanceof URL)
                    st.setURL(index, (URL) value);
                else if (value instanceof SQLXML)
                    st.setSQLXML(index, (SQLXML) value);
                else
                    st.setObject(index, value);
            }
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
        return st;
    }

    public static Scope populate(Scope scope, ResultSet resultSet) {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int index = 1; index <= metaData.getColumnCount(); index++) {
                String column = metaData.getColumnName(index);
                scope.put(column, resultSet.getObject(column));
            }
            return scope;
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    public static Scope duplicate(Scope scope, final String alias) {
        return new Scope() {{
            String prefix = alias == null || alias.isEmpty() ? "" : alias + ".";
            for (Entry<String, Object> entry : scope.entrySet())
                put(prefix + entry.getKey(), entry.getValue());
        }};
    }

    private Core() {
    }
}
