package com.github.ducoral.jutils;

import static com.github.ducoral.jutils.Core.*;

public class Oops extends RuntimeException {

    public static Oops of(Exception e) {
        return new Oops(e.getMessage(), e);
    }

    public static Oops of(String key, Object... args) {
        return new Oops(property(key, args));
    }

    public Oops(String message) {
        super(message);
    }

    public Oops(String message, Throwable cause) {
        super(message, cause);
    }

    public Oops(String message, Throwable cause, Object... arguments) {
        super(format(message, arguments), cause);
    }

    public Oops(String message, Object... arguments) {
        super(format(message, arguments));
    }
}
