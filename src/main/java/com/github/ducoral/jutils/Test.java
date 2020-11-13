package com.github.ducoral.jutils;

import static com.github.ducoral.jutils.Core.*;

public class Test {

    public static void main(String args[]) throws Exception {

        Object o = parseJson("{\"a\":123, \"b\":true, \"c\":[1, 2, 3, {\"opa\":\"teste\"}]}");

        System.out.println(o);


    }

}
