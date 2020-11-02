package com.github.ducoral.jutils;

import java.util.HashMap;

import static com.github.ducoral.jutils.Core.*;
import static com.github.ducoral.jutils.XML.*;

public class Test {

    public static void main(String[] args) {
        range(30, 25).forEach(System.out::println);
    }

    private static void testXMLs() {
        Element xml2 =
                element("raiz", attribute("x", "valor x"), attribute("y", "valor y"),
                        element("opa", "valor de opa", attribute("z", "valor z")),
                        element("tag-vazia"));

        System.out.println(xml2.toString(3));
        System.out.println();

        Element xml3 = root("<raiz x=\"valor x\" y=\"valor y\">" +
                    "<opa z=\"valor z\">valor de opa</opa>" +
                    "<tag-vazia/>" +
                "</raiz>");

        System.out.println(xml3.toString(3));
    }
}
