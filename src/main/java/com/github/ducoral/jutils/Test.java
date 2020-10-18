package com.github.ducoral.jutils;

import javax.crypto.spec.PSource;
import java.util.*;

import static com.github.ducoral.jutils.Core.*;
import static com.github.ducoral.jutils.XML.*;

public class Test {

    public static void main(String[] args) {
        Comma comma = new Comma();
        System.out.println("1a: " + comma);
        System.out.println("2a: " + comma);
        System.out.println("3a: " + comma);
        System.out.println("4a: " + comma);
    }

    private static void testJSONs() {
        Object map = new JsonMap() {{
            put("um", 12);
            put("dois", true);
            put("três", list("Teste", 10, false, null));
            put("quatro", "texto");
        }};

        System.out.println(json(map));

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
