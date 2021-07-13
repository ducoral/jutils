package com.github.ducoral.jutils;

import org.junit.jupiter.api.Test;

import static com.github.ducoral.jutils.Core.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OopsTest {

    @Test
    void test() {
        Oops oops = Oops.of("property.test", "one", "two");

        String expected = format("Message test parameter 1: %0; parameter 2: %1", "one", "two");

        assertEquals(expected, oops.getMessage());
    }

}