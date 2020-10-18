package com.github.ducoral.jutils;

import java.util.*;

import static com.github.ducoral.jutils.Core.*;
import static com.github.ducoral.jutils.XML.*;

public class Test {

    public static void main(String[] args) {
        List<?> lista = list("string", "1223", json(new JsonMap()));
        System.out.println(lista);
        testJSONs();
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
        Element xml1 = element("raiz");
        xml1.attributes.addAll(Arrays.asList(attribute("x", "valor x"), attribute("y", "valor y")));
        xml1.children.add(element("opa", "valor de opa", attribute("z", "valor z")));
        xml1.children.add(element("tag-vazia"));

        System.out.println(xml1.toString(3));
        System.out.println();

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
