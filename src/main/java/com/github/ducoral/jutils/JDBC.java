package com.github.ducoral.jutils;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.ducoral.jutils.Core.*;

public final class JDBC {

    public static Connection connection(String url, String user, String password) {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    public static Statement statement(Connection connection) {
        try {
            return connection.createStatement();
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    public static PreparedStatement prepare(Connection connection, Object sql, Object... args) {
        try {
            return parameter(connection.prepareStatement(sql.toString()), args);
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    public static PreparedStatement parameter(PreparedStatement st, Object... args) {
        for (int index = 0; index < args.length; index++)
            parameter(st, index, args[index]);
        return st;
    }

    public static PreparedStatement parameter(PreparedStatement st, int index, Object value) {
        try {
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
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
        return st;
    }

    public static boolean execute(Connection connection, Object sql) {
        try {
            return statement(connection).execute(sql.toString());
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    public static boolean execute(Connection connection, Object sql, Object... args) {
        try {
            return prepare(connection, sql, args).execute();
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    public static ResultSet select(Connection connection, Object sql, Object... args) {
        try {
            return prepare(connection, sql, args).executeQuery();
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    public static boolean create(Connection connection, String table, String... columns) {
        StringBuilder sql = new StringBuilder("create table ").append(table).append("(");
        Object comma = secondTimeReturns(", ");
        for (String column : columns)
            sql.append(comma).append(column);
        return execute(connection, sql.append(")"));
    }

    public static boolean drop(Connection connection, String table) {
        return execute(connection, "drop table " + table + " if exists");
    }

    public static void insert(Connection connection, String table, Object... values) {
        StringBuilder sql = new StringBuilder("insert into ").append(table).append(" values(");
        Object comma = secondTimeReturns(", ");
        times(values.length, () -> sql.append(comma).append('?'));
        execute(connection, sql.append(')'), values);
    }

    public static boolean update(Connection connection, String table, String condition, Map<Object, Object> values) {
        List<Object> params = new ArrayList<>();
        condition = extract(condition, params);
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("update ").append(table).append("set ");
        Object comma = secondTimeReturns(", ");
        for (Object column : values.entrySet())
            if (!params.contains(column)) {
                sql.append(comma).append(column).append(" = ?");
                args.add(values.get(column));
            }
        sql.append(' ').append(condition);
        for (Object param : params)
            args.add(values.get(param));
        return execute(connection, sql, args.toArray());
    }

    public static boolean next(ResultSet rs) {
        try {
            return rs.next();
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    private JDBC() {
    }
}