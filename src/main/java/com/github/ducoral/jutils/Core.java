package com.github.ducoral.jutils;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.ducoral.jutils.Constants.Strings.*;

/**
 *  Módulo principal da biblioteca.
 *
 *  Essa classe contém Classes, Interfaces e Métodos estáticos correspondentes à utilitários diversos
 *  e DSL que serve de base para implementação dos demais módulos da biblioteca.
 */
public final class Core {

    /**
     * Anotação para configurar a Classe que implementa determinada Interface.
     */
    @Target(value = ElementType.TYPE)
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface Bean {

        /**
         * Configuração de <i>Class</i> que implementa a Interface que está sendo anotada com <i>Bean</i>.
         *
         * @return Class Instância de <i>Class</i> que implenta a interface anotada com <i>Bean</i>
         */
        Class<?> type();
    }

    /**
     * Anotação para configurar que determinada Classe para que seja instanciada apena uma única vez, quando for
     * pelo método {@link #create(Class)}
     */
    @Target(value = ElementType.TYPE)
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface Singleton {
    }

    /**
     * <i>Enum</i> para configurar alinhamento de item na horizontal.
     */
    public enum Align { LEFT, CENTER, RIGHT }

    static PropertyResourceBundle properties = properties("jutils");

    static Map<Class<?>, Object> singletons = new HashMap<>();

    static String property(String key, Object... args) {
        return format(properties.getString(key), args);
    }

    /**
     * Retorna cópia do objeto especificado por parâmetro.
     *
     * Esse método cria cópia do objeto especificado por parâmetro chamando o método <i>clone</i>
     * via <i>Reflection</i>. Portanto, espera-se que o parâmetro seja um objeto contendo a implementação
     * do método <code>clone</code>.
     *
     * @param object Objeto do qual será clonado.
     * @param <T> Tipo do objeto
     * @return Nova instância de <code>T</code>, correspondente ao clone do objeto especificado por parâmetro.
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T object) {
        return (T) invoke(object, method(object.getClass(), "clone"));
    }

    /**
     * Cria uma nova instância da classe especificada por parâmetro, injetando as depêndicas da construtora,
     * conforme configurações via anotações {@link Bean} e {@link Singleton}.
     *
     * @param type <i>Class</i> correspondente ao tipo que deverá ser instanciado.
     * @return Nova instância da classe especificada por parâmetro.
     */
    public static Object create(Class<?> type) {
        return create(new Stack<>(), type);
    }

    private static Object create(Stack<String> scope, Class<?> type) {
        if (scope.contains(type.getName()))
            throw new Oops(property(CYCLIC_REFERENCE, scope.toString()));
        if (type.isInterface()) {
            Bean bean = Optional
                    .of(type.getAnnotation(Bean.class))
                    .orElseThrow(() -> new Oops(property(INTERFACE_MUST_BE_ANNOTATED_WITH, Bean.class.getName())));
            return create(scope, bean.type());
        }
        boolean isSingleton = type.getAnnotation(Singleton.class) != null;
        if (isSingleton && singletons.containsKey(type))
            return singletons.get(type);
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length == 0)
            throw new Oops(property(TYPE_DOES_NOT_CONTAIN_CONSTRUCTOR, type.getName()));
        else if (constructors.length > 1)
            throw new Oops(property(TYPE_CONTAINS_MORE_THAN_ONE_CONSTRUCTOR, type.getName(), Arrays.toString(constructors)));
        try {
            Parameter[] parameters = constructors[0].getParameters();
            Object[] args = new Object[parameters.length];
            for (int index = 0; index < args.length; index++)
                args[index] = create(push(clone(scope), type.getName()), parameters[index].getType());
            Object object = constructors[0].newInstance(args);
            if (isSingleton)
                singletons.put(type, object);
            return object;
        } catch (Exception e) {
            throw e instanceof Oops ? (Oops) e : Oops.of(e);
        }
    }

    /**
     * Cria um objeto com o método <i>toString()</i> implementado de tal forma que a <i>string</i> especificada
     * por parâmetro seja retornada a partir da segunda vez em que o método é chamado. Na primeira vez e que o
     * método <i>toString()</i> é chamado, é retornada uma <i>string</i> vazia.
     *
     * @param value String que será retornada a partir da segunda vez em que o método <i>toString()</i> do objeto
     *              retornado for chamado.
     * @return Objeto contendo implementação de <i>toString()</i> conforme regra descrita.
     */
    public static Object secondTimeReturns(String value) {
        return new Object() {
            int time = 1;
            public String toString() {
                return time++ == 1 ? "" : value;
            }
        };
    }

    /**
     * Definição de interface para concatenadores.
     */
    public interface Appendable {
        Appendable append(Object... values);
    }

    /**
     * Implementação de {@link Appendable} que utilizada determinado separador configurado por parâmetro
     * entre os itens concatenados.
     *
     * @param separator <i>String</i> correspondente ao separador dos itens concatenados.
     * @return Instância de {@link Appendable} configurada com o separado especificado por parâmetro.
     */
    public static Appendable appendable(String separator) {
        return new Appendable() {
            final StringBuilder builder = new StringBuilder();
            final Object delimiter = secondTimeReturns(separator);

            public Appendable append(Object... values) {
                for (Object value : values)
                    builder.append(delimiter).append(value);
                return this;
            }

            public String toString() {
                return builder.toString();
            }
        };
    }

    public static Stream<Integer> range(Integer start, Integer end) {
        List<Integer> range = new ArrayList<>();
        if (start > end)
            for (int item = start; item >= end; item--)
                range.add(item);
        else
            for (int item = start; item <= end; item++)
                range.add(item);
        return range.stream();
    }

    public static String str(int length, char fill) {
        return new String(new byte[length]).replace('\0', fill);
    }

    public static String str(Object value) {
        return value == null ? "" : value.toString();
    }

    public static String fill(String value, int width) {
        return fill(value, width, Align.LEFT, ' ');
    }

    public static String fill(String value, int width, Align align, char withChar) {
        int diff = width - value.length();
        if (diff < 1)
            return value;
        switch (align) {
            case LEFT:
                return value + str(diff, withChar);
            case RIGHT:
                return str(diff, withChar) + value;
            default:
                int half = diff / 2;
                return str(half, withChar) + value + str(diff - half, withChar);
        }
    }

    public static String replace(String str, String target, String replacement) {
        int index = str.indexOf(target);
        if (index > -1)
            str = str.substring(0, index) + replacement + str.substring(index + target.length());
        return str;
    }

    public static void replace(StringBuilder str, String target, String replacement) {
        int index = str.indexOf(target);
        str.replace(index, index + target.length(), replacement);
    }

    public static String format(String template, Pair... parameters) {
        return format(template, map(parameters));
    }

    public static String format(String template, Map<String, Object> scope) {
        StringBuilder result = new StringBuilder(template);
        parameters(template).forEach(param -> replace(result, param, str(scope.get(param))));
        return result.toString();
    }

    public static String format(String format, Object... args) {
        return String.format(format, args);
    }

    public static String format(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static String lower(Object value) {
        return String.valueOf(value).toLowerCase();
    }

    public static String upper(Object value) {
        return String.valueOf(value).toUpperCase();
    }

    public static boolean isOneOf(String value, String... values) {
        for (String item : values)
            if (Objects.equals(value, item))
                return true;
        return false;
    }

    public static boolean isPrimitiveType(Object value) {
        return Objects.isNull(value)
                || value instanceof Number
                || value instanceof CharSequence
                || value instanceof Date
                || value instanceof Boolean;
    }

    public static String json(Object value) {
        if (value instanceof List)
            return json((List<?>) value);
        else if (value instanceof Map)
            return json((Map<?, ?>) value);
        else if (value instanceof Time)
            return json(format((Time) value, property(Constants.Strings.JSON_TIME_FORMAT)));
        else if (value instanceof Date)
            return json(format((Date) value, property(Constants.Strings.JSON_DATETIME_FORMAT)));
        else if (isPrimitiveType(value)) {
            String str = String.valueOf(value);
            return value instanceof String ? '"' + str + '"' : str;
        } else try {
            Class<?> type = value.getClass();
            StringBuilder builder = new StringBuilder("{");
            Object comma = secondTimeReturns(",");
            for (Field field : type.getDeclaredFields())
                builder.append(comma).append('"').append(field.getName()).append("\":").append(json(field.get(value)));
            return builder.append('}').toString();
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    private static String json(List<?> list) {
        StringBuilder array = new StringBuilder("[");
        Object comma = secondTimeReturns(",");
        list.forEach(value -> array.append(comma).append(json(value)));
        return array.append("]").toString();
    }

    private static String json(Map<?, ?> map) {
        StringBuilder object = new StringBuilder("{");
        Object comma = secondTimeReturns(",");
        map.forEach((key, value) -> object
                .append(comma)
                .append(json(key))
                .append(':')
                .append(json(value)));
        return object.append("}").toString();
    }

    public static Object parseJson(String document) {
        return new JsonParser(new Scanner(document)).parse();
    }

    public static List<String> matches(Pattern pattern, String input) {
        return matches(pattern, input, 0);
    }

    public static List<String> matches(Pattern pattern, String input, int group) {
        List<String> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find())
            matches.add(matcher.group(group));
        return matches;
    }

    public static List<String> parameters(String template) {
        return matches(Constants.Patterns.PARAM, template);
    }

    public static byte[] bytes(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1)
                baos.write(buffer, 0, len);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static InputStream stream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    public static PropertyResourceBundle properties(String name) {
        ClassLoader loader = Core.class.getClassLoader();
        Locale locale = Locale.getDefault();
        URL resource = loader.getResource(format("%s_%s%s.properties", name, locale.getLanguage(), locale.getCountry()));
        if (resource == null)
            resource = loader.getResource(format("%s.properties", name));
        if (resource == null)
            throw new Oops("Could not load property file " + name);
        try {
            return new PropertyResourceBundle(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw Oops.of(e);
        }
    }

    @FunctionalInterface
    public interface Command {
        void execute();
    }

    public static void times(int times, Command command) {
        for (int i = 0; i < times; i++)
            command.execute();
    }

    public static List<Object> values(List<String> keys, Map<String, Object> map) {
        return new ArrayList<Object>() {{
            for (String key : keys)
                add(map.get(key));
        }};
    }

    public interface Pair {
        String key();
        Object value();
    }

    public static Pair pair(String key, Object value) {
        return new Pair() {
            public String key() {
                return key;
            }

            public Object value() {
                return value;
            }
        };
    }

    public static Map<String, Object> map(Pair... pairs) {
        return new HashMap<String, Object>() {{
            for (Pair pair : pairs)
                put(pair.key(), pair.value());
        }};
    }

    public interface MapBuilder {
        MapBuilder pair(String key, Object value);
        MapBuilder merge(Map<String, Object> map);
        MapBuilder rename(Function<String, String> renameKeyFunction);
        MapBuilder ignore();
        Map<String, Object> done();
    }

    public static MapBuilder map() {
        return map(new HashMap<>());
    }

    public static MapBuilder map(Map<String, Object> source) {
        return new MapBuilderImpl(source);
    }

    public static MapBuilder map(ResultSet rs) {
        try {
            HashMap<String, Object> resultSetMap = new HashMap<String, Object>() {{
                ResultSetMetaData metaData = rs.getMetaData();
                for (int index = 1; index <= metaData.getColumnCount(); index++)
                    put(metaData.getColumnName(index), rs.getObject(index));
            }};
            return map(resultSetMap);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static Method method(Class<?> type, String name, Class<?>... parameterTypes) {
        for (Method m : type.getMethods())
            if (m.getName().equals(name) && Arrays.equals(m.getParameterTypes(), parameterTypes))
                return m;
        return null;
    }

    public static Object invoke(Object object, Method method, Object... parameters) {
        try {
            method.setAccessible(true);
            return method.invoke(object, parameters);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public interface MockBuilder<T> {
        MockBuilder<T> returns(Function<T, ?> function);
        T done();
    }

    public static <T> MockBuilder<T> mock(Class<T> type) {
        return new MockBuilderImpl<>(type);
    }

    public static <T> Stack<T> push(Stack<T> stack, T value) {
        stack.push(value);
        return stack;
    }

    public static void set(Field field, Object object, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    public static <T> T build(Class<T> type, Object... args) {
        Iterator<Object> params = Arrays.asList(args).iterator();
        try {
            T object = type.newInstance();
            for (Field field : type.getDeclaredFields())
                if (params.hasNext())
                    set(field, object, params.next());
                else
                    break;
            return object;
        } catch (Exception e) {
            throw Oops.of(e);
        }
    }

    private Core() {
    }
}
