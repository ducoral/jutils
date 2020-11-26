package com.github.ducoral.jutils;

import java.util.regex.Pattern;

import static com.github.ducoral.jutils.Core.*;

public final class Constants {

    public static class Objects {
        public static final Pattern PARAM_PATTERN = Pattern.compile(property(Properties.PARAM_PATTERN));
    }

    public static class Properties {
        public static final String JSON_TIME_FORMAT = "json.time.format";
        public static final String JSON_DATETIME_FORMAT = "json.datetime.format";
        public static final String PARAM_PATTERN = "param.pattern";
        public static final String CYCLIC_REFERENCE = "cyclic.reference";
        public static final String INTERFACE_MUST_BE_ANNOTATED_WITH = "interface.must.be.annotated.with";
        public static final String TYPE_DOES_NOT_CONTAIN_CONSTRUCTOR = "type.does.not.contain.constructor";
        public static final String TYPE_CONTAINS_MORE_THAN_ONE_CONSTRUCTOR = "type.contains.more.than.one.constructo";
    }

    private Constants() {
    }
}
