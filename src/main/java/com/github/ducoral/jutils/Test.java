package com.github.ducoral.jutils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.ducoral.jutils.Core.*;
import static com.github.ducoral.jutils.XML.*;

public class Test {

    public static void main(String[] args) {
        Pattern patter = Pattern.compile("\\$\\{((\\w+\\.?\\w*))}");
        Matcher matcher = patter.matcher("um: ${um}, dois: ${dois}, tres: ${tres}");
        while (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++)
                System.out.println(i + ": " + matcher.group(i));
            System.out.println();
        }
    }

    private static void testJSONs() {
        Object map = new HashMap<String, Object>() {{
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
