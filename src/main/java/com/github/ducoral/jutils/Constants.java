package com.github.ducoral.jutils;

import java.util.regex.Pattern;

import static com.github.ducoral.jutils.Core.*;

class Constants {

    static class Patterns {
        public static final Pattern PARAM = Pattern.compile(property(Properties.PARAM_REGEX));
    }

    static class Properties {
        public static final String JSON_TIME_FORMAT = "json.time.format";
        public static final String JSON_DATETIME_FORMAT = "json.datetime.format";
        public static final String PARAM_REGEX = "param.regex";
        public static final String CYCLIC_REFERENCE = "cyclic.reference";
        public static final String INTERFACE_MUST_BE_ANNOTATED_WITH = "interface.must.be.annotated.with";
        public static final String TYPE_DOES_NOT_CONTAIN_CONSTRUCTOR = "type.does.not.contain.constructor";
        public static final String TYPE_CONTAINS_MORE_THAN_ONE_CONSTRUCTOR = "type.contains.more.than.one.constructor";
        public static final String INCORRECT_OPERATOR = "incorrect.operator";
        public static final String INVALID_CHARACTER="invalid.character";
        public static final String INVALID_DECIMAL="invalid.decimal";
        public static final String INVALID_TOKEN="invalid.token";
        public static final String INVALID_EXPECTED_TOKEN="invalid.expected.token";
        public static final String STRING_NOT_CLOSED="string.not.closed";
    }
}
