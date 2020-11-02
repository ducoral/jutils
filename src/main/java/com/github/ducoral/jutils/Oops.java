package com.github.ducoral.jutils;

import static com.github.ducoral.jutils.Core.*;

public class Oops extends RuntimeException {

    public static Oops of(Exception e) {
        return new Oops(e.getMessage(), e);
    }

    public Oops(String message, Object... arguments) {
        super(format(message, arguments));
    }

    public Oops(String message, Throwable cause, Object... arguments) {
        super(format(message, arguments), cause);
    }
}
