package com.github.ducoral.jutils;

import java.util.regex.Pattern;

public final class Constants {

    public static final class RegEx {

        public static final String PARAM = "\\$\\{(\\w+(\\.\\w+)*)}";

        private RegEx() {
        }

    }

    public static final class Patterns {

        public static final Pattern PARAM = Pattern.compile(RegEx.PARAM);

        private Patterns() {
        }

    }

    public static class Strings {

        public static final String JSON_TIME_FORMAT = "json.time.format";
        public static final String JSON_DATETIME_FORMAT = "json.datetime.format";
        public static final String CYCLIC_REFERENCE = "cyclic.reference";
        public static final String INTERFACE_MUST_BE_ANNOTATED_WITH = "interface.must.be.annotated.with";
        public static final String TYPE_CONTAINS_MORE_THAN_ONE_CONSTRUCTOR = "type.contains.more.than.one.constructor";
        public static final String TYPE_DOES_NOT_CONTAIN_CONSTRUCTOR = "type.does.not.contain.constructor";
        public static final String TYPE_DOES_NOT_CONTAIN_CLONE_METHOD = "type.does.not.contain.clone.method";
        public static final String INCORRECT_OPERATOR = "incorrect.operator";
        public static final String INVALID_CHARACTER = "invalid.character";
        public static final String INVALID_DECIMAL = "invalid.decimal";
        public static final String INVALID_TOKEN = "invalid.token";
        public static final String INVALID_EXPECTED_TOKEN = "invalid.expected.token";
        public static final String STRING_NOT_CLOSED = "string.not.closed";
        public static final String XML_MISSING_TAG = "xml.missing.tag";
        public static final String XML_EXPECTED_TAG = "xml.expected.tag";

        private Strings() {
        }

    }

    private Constants() {
    }
    
}
