package com.github.ducoral.jutils;

import com.github.ducoral.jutils.Scanner.Token;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.ducoral.jutils.Constants.Strings.INVALID_EXPECTED_TOKEN;
import static com.github.ducoral.jutils.Constants.Strings.INVALID_TOKEN;

/* ================================================
           json ::= empty
                  | value

          value ::= object
                  | array
                  | literal

         object ::= '{' properties '}'

     properties ::= empty
                  | property properties_rest

       property ::= string ':' value

properties_rest ::= empty
                  | ',' property properties_rest

          array ::= '[' items ']'

          items ::= empty
                  | value items_rest

     items_rest ::= empty
                  | ',' value items_rest

        literal ::= string
                  | integer
                  | decimal
                  | boolean
                  | null
================================================ */
class JsonParser {

    final Scanner scanner;

    JsonParser(Scanner scanner) {
        this.scanner = scanner;
    }

    Object parse() {
        Object value = scanner.token == Token.EOF ? null : parseValue();
        accept(Token.EOF);
        return value;
    }

    private Object parseValue() {
        switch (scanner.token) {
            case OPEN_BRACES: return parseObject();
            case OPEN_BRACKETS: return parseArray();
            case STRING: return accept(Token.STRING);
            case INTEGER: return new BigInteger(accept(Token.INTEGER));
            case DECIMAL: return new BigDecimal(accept(Token.DECIMAL));
            case BOOLEAN: return Boolean.valueOf(accept(Token.BOOLEAN));
            case NULL: accept(Token.NULL); return null;
        }
        throw Oops.of(INVALID_TOKEN, scanner.token);
    }

    private Object parseObject() {
        accept(Token.OPEN_BRACES);
        Map<String, Object> object = new LinkedHashMap<>();
        while (isNotTokenOrEOF(Token.CLOSE_BRACES)) {
            parseKeyValue(object);
            while (scanner.token == Token.COMMA) {
                accept(Token.COMMA);
                parseKeyValue(object);
            }
        }
        accept(Token.CLOSE_BRACES);
        return object;
    }

    private void parseKeyValue(Map<String, Object> object) {
        String key = accept(Token.STRING);
        accept(Token.COLON);
        object.put(key, parseValue());
    }

    private Object parseArray() {
        accept(Token.OPEN_BRACKETS);
        List<Object> array = new ArrayList<>();
        while (isNotTokenOrEOF(Token.CLOSE_BRACKETS)) {
            array.add(parseValue());
            while (scanner.token == Token.COMMA) {
                accept(Token.COMMA);
                array.add(parseValue());
            }
        }
        accept(Token.CLOSE_BRACKETS);
        return array;
    }

    private boolean isNotTokenOrEOF(Token token) {
        return scanner.token != token && scanner.token != Token.EOF;
    }

    private String accept(Token token) {
        if (scanner.token != token)
            throw Oops.of(INVALID_EXPECTED_TOKEN, scanner.token, token);
        String lexeme = scanner.lexeme;
        scanner.scan();
        return lexeme;
    }
}
