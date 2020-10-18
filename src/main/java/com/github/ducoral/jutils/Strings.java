package com.github.ducoral.jutils;

public final class Strings {

    public enum Align { LEFT, CENTER, RIGHT}

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

    private Strings() {
    }
}
