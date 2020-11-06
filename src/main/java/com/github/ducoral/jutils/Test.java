package com.github.ducoral.jutils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.github.ducoral.jutils.Core.*;

public class Test {

    public static class Teste {
        String campo1;
        Integer campo2;
        Date campo3;
        List<String> valores;
    }

    public static void main(String[] args) {
        Teste teste = build(Teste.class, "valor campo1", 125, new Date(), Arrays.asList("um", "dois", "tres"));

        System.out.println(json(teste));


    }

}
