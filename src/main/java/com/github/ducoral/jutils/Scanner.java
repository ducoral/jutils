package com.github.ducoral.jutils;

import java.util.function.Predicate;

import static java.lang.Character.*;
import static com.github.ducoral.jutils.Core.*;
import static com.github.ducoral.jutils.Constants.Strings.*;

public class Scanner {

    public enum Token {
        IDENTIFIER,
        INTEGER,
        DECIMAL,
        STRING,
        BOOLEAN,
        OPERATOR,
        QUESTION_MARK,
        COMMA,
        COLON,
        DOT,
        OPEN_PARENTHESES,
        CLOSE_PARENTHESES,
        OPEN_BRACKETS,
        CLOSE_BRACKETS,
        OPEN_BRACES,
        CLOSE_BRACES,
        NULL,
        ERROR,
        EOF
    }

    public static final int CHAR_EOF = 0;

    public final String expression;

    public int position = 0;

    public Token token = Token.EOF;

    public String lexeme = "";

    public String error = "";

    public Scanner(String expression) {
        this.expression = expression;
        scan();
    }

    public void scan() {
        lexeme = "";
        error = "";

        while (isSpaceChar(current()))
            next();

        if (current() == CHAR_EOF)
            token = Token.EOF;
        else if (current() == '-' || isDigit(current()))
            parseMinusOperatorOrNumber();
        else if (current() == '_' || isLetter(current()))
            parseNullBooleanOrIdentifier();
        else if (current() == '\'' || current() == '"')
            parseString();
        else if (current() == '.') {
            accept();
            token = Token.DOT;
        } else if (isCurrentOneOf("()[]{},?:")) {
            accept();
            switch (previous()) {
                case '(' : token = Token.OPEN_PARENTHESES; break;
                case ')' : token = Token.CLOSE_PARENTHESES; break;
                case '[' : token = Token.OPEN_BRACKETS; break;
                case ']' : token = Token.CLOSE_BRACKETS; break;
                case '{' : token = Token.OPEN_BRACES; break;
                case '}' : token = Token.CLOSE_BRACES; break;
                case ',' : token = Token.COMMA; break;
                case '?' : token = Token.QUESTION_MARK; break;
                case ':' : token = Token.COLON; break;
            }
        } else if (isCurrentOneOf("+*/=!&|><")) {
            accept();
            if (previous() == '(')
                token = Token.OPEN_PARENTHESES;
            else if (previous() == ')')
                token = Token.CLOSE_PARENTHESES;
            else if (previous() == '=' && current() != '=')
                error(INCORRECT_OPERATOR, "=", "==");
            else if (previous() == '!' && current() != '=')
                error(INCORRECT_OPERATOR, "!", "!=");
            else if (previous() == '&' && current() != '&')
                error(INCORRECT_OPERATOR, "&", "&&");
            else if (previous() == '|' && current() != '|')
                error(INCORRECT_OPERATOR, "|", "||");
            else {
                if (isCurrentOneOf("=&|"))
                    accept();
                token = Token.OPERATOR;
            }
        } else
            error(INVALID_CHARACTER, current());
    }

    private void parseMinusOperatorOrNumber() {
        accept();
        if (previous() == '-' && !isDigit(current()))
            token = Token.OPERATOR;
        else {
            acceptWhile(Character::isDigit);
            if (current() == '.') {
                accept();
                acceptWhile(Character::isDigit);
                if (isCurrentOneOf("eE")) {
                    accept();
                    if (isCurrentOneOf("-+"))
                        accept();
                    if (isDigit(current())) {
                        acceptWhile(Character::isDigit);
                        token = Token.DECIMAL;
                    } else
                        error(INVALID_DECIMAL, lexeme);
                } else
                    token = Token.DECIMAL;
            } else
                token = Token.INTEGER;
        }
    }

    private void parseNullBooleanOrIdentifier() {
        acceptWhile(ch -> ch == '_' || isLetter(ch) || isDigit(ch));
        if (lexeme.equals("null"))
            token = Token.NULL;
        else if (lexeme.equals("true") || lexeme.equals("false"))
            token = Token.BOOLEAN;
        else
            token = Token.IDENTIFIER;
    }

    private void parseString() {
        char delimiter = current();
        next();
        while (current() != CHAR_EOF && current() != delimiter) {
            if (current() == '\\')
                next();
            accept();
        }
        if (current() == delimiter) {
            next();
            token = Token.STRING;
        } else
            error(STRING_NOT_CLOSED, lexeme);
    }

    private void next() {
        if (position < expression.length())
            position++;
    }

    private void accept() {
        lexeme += current();
        next();
    }

    private void acceptWhile(Predicate<Character> predicate) {
        while (predicate.test(current()))
            accept();
    }

    private boolean isCurrentOneOf(String chars) {
        return chars.indexOf(current()) > -1;
    }

    private char current() {
        return position < expression.length()
                ? expression.charAt(position)
                : CHAR_EOF;
    }

    private char previous() {
        return lexeme.isEmpty() ? 0 : lexeme.charAt(lexeme.length() - 1);
    }

    private void error(String key, Object... args) {
        token = Token.ERROR;
        error = property(key, args);
    }
}