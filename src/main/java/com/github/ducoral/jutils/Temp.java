package com.github.ducoral.jutils;

public class Temp {

    public static void main(String[] args) {
        for (String param : Core.parameters(" ${uma} skfdf ${duas} aslkdf ${tres}"))
            System.out.println(param);
    }
}
