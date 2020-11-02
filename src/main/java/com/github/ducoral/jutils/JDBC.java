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
            throw Oops.of(e);
        }
    }

    public static Statement statement(Connection connection) {
        try {
            return connection.createStatement();
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static PreparedStatement prepare(Connection connection, Object sql, Object... args) {
        try {
            return parameter(connection.prepareStatement(sql.toString()), args);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static PreparedStatement parameter(PreparedStatement st, Object... args) {
        try {
            for (int index = 0; index < args.length; index++)
                st.setObject(index + 1, args[index]);
        } catch (Exception e) {
            throw Oops.of(e);
        }
        return st;
    }

    public static boolean execute(Connection connection, Object sql) {
        try {
            return statement(connection).execute(sql.toString());
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static boolean execute(Connection connection, Object sql, Object... args) {
        try {
            return prepare(connection, sql, args).execute();
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static void commit(Connection connection) {
        try {
            connection.commit();
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static ResultSet select(Connection connection, Object sql, Object... args) {
        try {
            return prepare(connection, sql, args).executeQuery();
        } catch (Exception e) {
            throw Oops.of(e);
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

    public static boolean insert(Connection connection, String table, Object... values) {
        StringBuilder sql = new StringBuilder("insert into ").append(table).append(" values(");
        Object comma = secondTimeReturns(", ");
        times(values.length, () -> sql.append(comma).append('?'));
        return execute(connection, sql.append(')'), values);
    }

    public static boolean next(ResultSet rs) {
        try {
            return rs.next();
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static byte[] getBytes(ResultSet rs, String column) {
        try {
            return rs.getBytes(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static String getString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Short getShort(ResultSet rs, String column) {
        try {
            return rs.getShort(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Integer getInteger(ResultSet rs, String column) {
        try {
            return rs.getInt(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Long getLong(ResultSet rs, String column) {
        try {
            return rs.getLong(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Float getFloat(ResultSet rs, String column) {
        try {
            return rs.getFloat(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Double getDouble(ResultSet rs, String column) {
        try {
            return rs.getDouble(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static BigDecimal getBigDecimal(ResultSet rs, String column) {
        try {
            return rs.getBigDecimal(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Date getDate(ResultSet rs, String column) {
        try {
            return rs.getDate(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Time getTime(ResultSet rs, String column) {
        try {
            return rs.getTime(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Boolean getBoolean(ResultSet rs, String column) {
        try {
            return rs.getBoolean(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Object getObject(ResultSet rs, String column) {
        try {
            return rs.getObject(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Blob getBlob(ResultSet rs, String column) {
        try {
            return rs.getBlob(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Clob getClob(ResultSet rs, String column) {
        try {
            return rs.getClob(column);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    private JDBC() {
    }
}