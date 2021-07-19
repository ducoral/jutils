package com.github.ducoral.jutils;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.ducoral.jutils.Constants.Strings.*;

/**
 *  Módulo principal da biblioteca.
 *  <br/><br/>
 *  Essa classe contém Classes, Interfaces e Métodos estáticos correspondentes à utilitários diversos
 *  e DSL que serve de base para implementação dos demais módulos da biblioteca.
 */
public final class Core {

    /**
     * Anotação para configurar Interface com a referência para a classe que a implementa.
     * <br/><br/>
     * Quando uma nova instância de determinada interface for solicitada ao método {@link #create(Class)},
     * será a classe configurada em {@link Bean#type() type} que será instanciada e retornada.
     * <br/><br/>
     * Por exemplo, a interface <code>Config</code> e sua implementação correspondente <code>ConfigImpl</code> poderiam
     * estar declarados da seguinte forma:<br/><br/>
     * <pre>
     *   {@literal @}Bean(type = ConfigImpl.class)
     *    interface Config {
     *      String value(String property);
     *    }
     *
     *    class ConfigImpl implements Config {
     *      Map&lt;String, String&gt; properties = new HashMap<>();
     *
     *     {@literal @}Overrite
     *      String value(String property) {
     *        return properties.get(property);
     *      }
     *    }
     * </pre>
     *
     * Sendo assim, ao solicitar uma instância de <code>Config</code>, chamando {@link #create(Class) create(Config.class)},
     * seria retornada uma instância de <code>ConfigImpl</code>.
     */
    @Target(value = ElementType.TYPE)
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface Bean {

        /**
         * <i>Class</i> da implementação da interface anotada com {@link Bean}.
         *
         * @return instância de <i>Class</i> que implementa a interface anotada.
         */
        Class<?> type();
    }

    /**
     * Determinada classe anotada com {@link Singleton} terá apenas uma única instância
     * criada através do método {@link #create(Class)}.
     * <br/><br/>
     *
     * Por exemplo, se determinada classe <code>Config</code>, anotada com {@link Singleton}:
     * <pre>
     *     @ Singleton
     *     class Config {
     *     }
     * </pre>
     *
     * for instanciada N vezes, como da seguinte forma:
     *
     * <pre>
     *     Config configA = create(Config.class);
     *     Config configB = create(Config.class);
     *     Config configC = create(Config.class);
     * </pre>
     *
     * todas as instâncias <code>Config</code> serão referência para o mesmo objeto. Ou seja, as variáveis
     * <code>configA</code>, <code>configB</code> e <code>configC</code> serão todas a mesma instância de
     * <code>Config</code>.
     */
    @Target(value = ElementType.TYPE)
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface Singleton {
    }

    /**
     * <i>Enum</i> para configurar alinhamento de item na horizontal.
     */
    public enum Align { LEFT, CENTER, RIGHT }

    static PropertyResourceBundle properties = properties(Core.class.getClassLoader(),"jutils");

    static Map<Class<?>, Object> singletons = new HashMap<>();

    static String property(String key, Object... args) {
        return format(properties.getString(key), args);
    }

    /**
     * Retorna cópia do objeto especificado por parâmetro, chamando o método {@link Object#clone()}
     * via <i>Reflection</i>. Portanto, espera-se que o parâmetro seja uma instância de
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Cloneable.html">Cloneable</a>.
     *
     * @param object Objeto que será clonado.
     * @param <T> tipo do objeto clonado.
     * @return nova instância de <code>T</code>, correspondente ao clone do objeto especificado por parâmetro.
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T object) {
        Method clone = method(object.getClass(), "clone");
        if (clone == null)
            throw Oops.of(TYPE_DOES_NOT_CONTAIN_CLONE_METHOD, object.getClass());
        return (T) invoke(object, clone);
    }

    /**
     * Cria uma nova instância da classe especificada por parâmetro, injetando as depêndicas da construtora,
     * conforme configurações via anotações {@link Bean} e {@link Singleton}.
     *
     * @param type <i>Class</i> correspondente ao tipo que deverá ser instanciado.
     * @return nova instância da classe especificada por parâmetro.
     */
    public static Object create(Class<?> type) {
        return create(new ArrayDeque<>(), type);
    }

    private static Object create(Deque<String> scope, Class<?> type) {
        if (scope.contains(type.getName()))
            throw Oops.of(property(CYCLIC_REFERENCE, scope.toString()));
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
            throw Oops.of(TYPE_DOES_NOT_CONTAIN_CONSTRUCTOR, type.getName());
        else if (constructors.length > 1)
            throw Oops.of(TYPE_CONTAINS_MORE_THAN_ONE_CONSTRUCTOR, type.getName(), Arrays.toString(constructors));
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
            throw e instanceof Oops
                    ? (Oops) e
                    : Oops.of(e);
        }
    }

    /**
     * Cria um objeto com o método <i>toString()</i> implementado de tal forma que a <i>string</i> especificada
     * por parâmetro seja retornada a partir da segunda vez em que o método é chamado. Na primeira vez em que o
     * método <i>toString()</i> é chamado, é retornada uma <i>string</i> vazia.
     *
     * @param value String que será retornada a partir da segunda vez em que o método <i>toString()</i> do objeto
     *              retornado for chamado.
     * @return objeto contendo implementação de <i>toString()</i> conforme regra descrita.
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
     * @return instância de {@link Appendable} configurada com o separador especificado por parâmetro.
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

            @Override
            public String toString() {
                return builder.toString();
            }
        };
    }

    /**
     * Retorna nova instância de {@link String} com tamanho <code>length</code>, preenchido
     * com o caratere <code>fill</code>, especificados por parâmetro.
     *
     * @param length int correspondente ao comprimento da {@link String} que será retornada.
     * @param fill char de preenchimento da {@link String}
     * @return nova {@link String} correspondente aos parâmetros especificados.
     */
    public static String str(int length, char fill) {
        return new String(new byte[length]).replace('\0', fill);
    }

    /**
     * Retorna instância de {@link String} correspondente ao resultado do método
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#toString">Object.toString()</a>,
     * invocado no objeto <code>value</code>, especifiado por parâmetro.
     * @param value objeto que será convertido para {@link String}
     * @return {@link String} correspondente ao objeto especificado por parâmetro. Se o valor de <code>value</code>
     * for <code>null</code>, então será retornada uma {@link String} vazia (<code>""</code>).
     */
    public static String str(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * Retorna {@link String} de comprimento <code>width</code>, com <code>value</code> alinhado conforme
     * <code>align</code>, com a diferença de tamanho (<code>width</code> - <code>value.length()</code>) preenchida
     * com <code>withChar</code>.
     * @param value {@link String} que será alinhada na nova {@link String} retornada, conforme parâmetros.
     * @param width <code>int</code> correspondente ao comprimento da nova {@link String} retornada.
     * @param align alinhamento horizontal de <code>value</code> referente à nova {@link String} retornada.
     * @param withChar <code>char</code> utilizado para preencher a diferença de comprimento na nova {@link String}.
     * @return nova {@link String} correspondente ao parâmetros especificados.
     */
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

    /**
     * Retorna nova {@link String} de comprimento <code>width</code>, contendo a {@link String}
     * <code>value</code> alinhada à esquerda, contendo a diferença de comprimento preenchida com o caracter
     * de espaço (<code>' '</code>).
     * @param value {@link String} que será alinhada à esquerda na nova {@link String} retornada.
     * @param width <code>int</code> correspondente ao comprimento da nova {@link String} retornada.
     * @return nova {@link String} correspondente ao parâmetros especificados.
     */
    public static String fill(String value, int width) {
        return fill(value, width, Align.LEFT, ' ');
    }

    /**
     * Substitui todas as ocorrências de <code>target</code> em <code>str</code> por <code>replacement</code>.
     * @param str <code>StringBuilder</code> que será afetado pela substituição de <i>strings</i>.
     * @param target {@link String} que será substituída.
     * @param replacement {@link String} que será utilizada na substituição.
     */
    public static void replace(StringBuilder str, String target, String replacement) {
        int index = str.indexOf(target);
        while (index > -1) {
            str.replace(index, index + target.length(), replacement);
            index = str.indexOf(target);
        }
    }

    /**
     * Formata e retorna a {@link String} <code>template</code> de acordo com as instâncias de
     * {@link Pair}, especificados por parâmetro.
     * <br/><br/>
     * Os parâmetros são identificados conforme expressão regular configurada em {@link Constants.RegEx#PARAM}
     * @param template {@link String} que terá o parâmetros substituídos pelos respectivos valores.
     * @param parameters instâncias de {@link Pair} correspondente aos parâmetros e seus respectivos valores.
     * @return {@link String} resultante da formatação da <code>template</code>, conforme <code>parameters</code>
     *         especificados.
     */
    public static String format(String template, Pair... parameters) {
        return format(template, map(parameters));
    }

    /**
     * Formata e retorna a {@link String} <code>template</code> de acordo com os parâmetros configurados em
     * {@link Map} <code>scope</code>, especificado por parâmetro.
     * <br/><br/>
     * Os parâmetros são identificados conforme expressão regular configurada em {@link Constants.RegEx#PARAM}
     * @param template {@link String} que terá o parâmetros substituídos pelos respectivos valores.
     * @param scope instâncias de {@link Map} contendo os parâmetros e seus respectivos valores.
     * @return {@link String} resultante da formatação da <code>template</code>, conforme <code>scope</code>
     *         especificado por parâmetro.
     */
    public static String format(String template, Map<String, Object> scope) {
        StringBuilder result = new StringBuilder(template);
        parameters(template).forEach(param -> replace(result, param, str(scope.get(param))));
        return result.toString();
    }

    /**
     * Formata e retorna a {@link String} <code>template</code> de acordo com os parâmetros especifidos em
     * <code>args</code>.
     * <br/><br/>
     * Esse método espera que os parâmetros em <code>template</code> tenham a seguinte sintaxe %&lt;número&gt;, de tal
     * forma que &lt;número&gt; representa a posição do parâmetro especificado em <code>args</code>.
     * <br/>
     * @param template {@link String} que será formatada.
     * @param args <i>array</i> de {@link Object} correspondentes aos parâmetros na formação de <code>template</code>.
     * @return {@link String} <code>template</code> formatada conforme parâmetros especificados em <code>args</code>.
     */
    public static String format(String template, Object... args) {
        String formatted = template;
        for (int position = 0; position < args.length; position++) {
            String parameter = "%" + position;
            if (formatted.contains(parameter))
                formatted = formatted.replace(parameter, str(args[position]));
        }
        return formatted;
    }

    /**
     * Retorna {@link String} contendo <code>date</code> formatada conforme <code>format</code>.
     * @param date instância de {@link Date} que será formatada.
     * @param format {@link String} contendo o formato que será aplicado na formatação da data.
     * @return {@link String} resultante da formatação da data especificada por parâmetro.
     */
    public static String format(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Retorna o objeto <code>value</code>, especificado por parâmetro, convertido para {@link String},
     * com os caracteres convertidos para minúsculo.
     * @param value {@link Object} do qual será convertido para {@link String} com caracteres em minúsculo.
     * @return {@link String} correspondente ao <code>value</code>, especificado por parâmetro, convertido para
     *         {@link String}, com os caracteres convertidos para minúsculo.
     */
    public static String lower(Object value) {
        return String.valueOf(value).toLowerCase();
    }

    /**
     * Retorna o objeto <code>value</code>, especificado por parâmetro, convertido para {@link String},
     * com os caracteres convertidos para maiúsculo.
     * @param value {@link Object} do qual será convertido para {@link String} com caracteres em maiúsculo.
     * @return {@link String} correspondente ao <code>value</code>, especificado por parâmetro, convertido para
     *         {@link String}, com os caracteres convertidos para maiúsculo.
     */
    public static String upper(Object value) {
        return String.valueOf(value).toUpperCase();
    }

    /**
     * Verifica se a {@link String} <code>value</code> contém pelo menos uma das <code>Strings</code>
     * especifica em <code>values</code>.
     * @param value {@link String} em que será verificada a existência de pelo menos um item do <i>array</i> <code>values</code>.
     * @param values <i>array</i> contendo as <code>Strings</code> que serão verificadas.
     * @return <code>true</code> se <code>value</code> contiver pelo menos uma das <code>Strings</code> especificadas em <code>values</code>.
     */
    public static boolean isOneOf(String value, String... values) {
        for (String item : values)
            if (Objects.equals(value, item))
                return true;
        return false;
    }

    /**
     * Verifica se <code>value</code> é valor <code>null</code> ou instância de classes correspondentes
     * ao tipos primitivos do Java, tais como {@link Number}, {@link CharSequence} e {@link Boolean}.
     * @param value {@link Object} que terá o tipo verificado.
     * @return <code>true</code> <code>value</code> for instância de classe de tipo primitivo do Java.
     */
    public static boolean isNullOrPrimitiveType(Object value) {
        return Objects.isNull(value)
                || value instanceof Number
                || value instanceof CharSequence
                || value instanceof Boolean;
    }

    /**
     * Converte objeto Java em de objeto JSON.
     * @param value objeto Java que será convertiddo para objeto JSON.
     * @return {@link String} no formato JSON contendo o objeto especificado em <code>value</code>.
     */
    public static String json(Object value) {
        if (value instanceof List)
            return json((List<?>) value);
        else if (value instanceof Map)
            return json((Map<?, ?>) value);
        else if (value instanceof Time)
            return json(format((Time) value, property(Constants.Strings.JSON_TIME_FORMAT)));
        else if (value instanceof Date)
            return json(format((Date) value, property(Constants.Strings.JSON_DATETIME_FORMAT)));
        else if (value instanceof LocalDate)
            return json(((LocalDate) value).toString());
        else if (isNullOrPrimitiveType(value)) {
            String str = String.valueOf(value);
            return value instanceof CharSequence ? '"' + str + '"' : str;
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

    /**
     * Converte um {@link List} em <i>array</i> JSON.
     * @param list {@link List} que será convertido em <i>array</i> JSON
     * @return {@link String} contendo o <i>array</i> JSON correspondente ao {@link List} especificado por parâmetro.
     */
    private static String json(List<?> list) {
        StringBuilder array = new StringBuilder("[");
        Object comma = secondTimeReturns(",");
        list.forEach(value -> array.append(comma).append(json(value)));
        return array.append("]").toString();
    }

    /**
     * Converte um {@link Map} em objeto JSON.
     * @param map {@link Map} que será convertido para objeto JSON.
     * @return String contendo o objeto JSON correspondente ao {@link Map} especificado por parâmetro.
     */
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

    /**
     * Converte {@link String} no formato JSON para instância de {@link Object}.
     * @param document {@link String} no formado JSON.
     * @return instância de {@link Object} correspondente o documento JSON especificado por parâmetro.
     */
    public static Object parseJson(String document) {
        return new JsonParser(new Scanner(document)).parse();
    }

    /**
     * Aplica o padrão <code>pattern</code> no <code>input</code>, especificados por parâmetro,
     * e retorna lista de {@link String} contendo as ocorrências correspondentes.
     *
     * @param pattern instância de {@link Pattern} correspondente ao padrão a ser identificado.
     * @param input {@link String} em que serão identificadas as ocorrênicas do padrão especificado.
     * @return lista de <code>String/code> contendo os padrões identificados na {@link String} especificada.
     */
    public static List<String> matches(Pattern pattern, String input) {
        return matches(pattern, input, 0);
    }

    /**
     * Aplica o padrão <code>pattern</code> no <code>input</code>, ambos especificados por parâmetro,
     * e retorna lista de {@link String} contendo as ocorrências correspondente ao grupo especificado em
     * <code>group</code>.
     *
     * @param pattern instância de {@link Pattern} correspondente ao padrão a ser identificado.
     * @param input {@link String} em que serão identificadas as ocorrênicas do padrão especificado.
     * @param group int correspondente ao grupo da expressão regular que será retornado.
     * @return lista de {@link String} contendo os padrões identificados na {@link String} <code>input</code>>,
     *         corresponentes ao grupo <code>group</code>, especificados por parâmetro.
     */
    public static List<String> matches(Pattern pattern, String input, int group) {
        List<String> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find())
            matches.add(matcher.group(group));
        return matches;
    }

    /**
     * Identifica e retorna os parâmetros existentes no <code>template</code> especificado por parâmetro.
     * <br/></br>
     * Os parãmetros são identificados conforme expressão regular configurada em {@link Constants.RegEx#PARAM}
     *
     * @param template {@link String} da qual os parâmetros serão identificados e retornados.
     *
     * @return conjunto de <code>Strings</code> contendo os parâmetros identificados no <code>template</code> especificado.
     */
    public static Set<String> parameters(String template) {
        return new HashSet<>(matches(Constants.Patterns.PARAM, template));
    }

    /**
     * Retorna <i>array</i> de <code>byte</code> correspondente ao conteúdo de <code>input</code> especificado por
     * parâmetro.
     * @param input instância de {@link InputStream} que será convertida para <code>array</code> de <code>bytes</code>
     * @return <i>array</i> de <code>bytes</code> correspondente ao conteúdo de <code>input</code> especificado por
     *         parâmetro.
     */
    public static byte[] bytes(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1)
                baos.write(buffer, 0, len);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new Oops(e.getMessage(), e);
        }
    }

    /**
     * Retorna instância de {@link InputStream} contendo os <code>bytes</code> especificados por parâmetro.
     * @param bytes <i>array</i> de <code>byte</code> quer será encapsulado na instância de {@link InputStream}.
     * @return instância de {@link InputStream} contendo os <code>bytes</code> especificados por parâmetro.
     */
    public static InputStream stream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Retorna nova instância de {@link PropertyResourceBundle} contendo o arquivo <code>*.properties</code> com
     * o <code>nome</code> especificado.
     * @param loader instância de {@link ClassLoader} a partir do qual o arquivo <code>*.properties</code> será carregado.
     * @param name {@link String} contendo o nome do arquivo <code>*.properties</code> será carregado.
     * @return instância de {@link PropertyResourceBundle}.
     */
    public static PropertyResourceBundle properties(ClassLoader loader, String name) {
        Locale locale = Locale.getDefault();
        URL resource = loader.getResource(format("%0_%1%2.properties", name, locale.getLanguage(), locale.getCountry()));
        if (resource == null)
            resource = loader.getResource(format("%0.properties", name));
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

        default Command andThen(Command after) {
            Objects.requireNonNull(after);
            return () -> {
                this.execute();
                after.execute();
            };
        }
    }

    public static void times(int times, Command command) {
        for (int i = 0; i < times; i++)
            command.execute();
    }

    public static List<Object> values(List<String> keys, Map<String, Object> map) {
        List<Object> list = new ArrayList<>();
        for (String key : keys)
            list.add(map.get(key));
        return list;
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
        Map<String, Object> params = new HashMap<>();
        for (Pair pair : pairs)
            params.put(pair.key(), pair.value());
        return params;
    }

    public interface MapBuilder {
        MapBuilder pair(String key, Object value);
        MapBuilder merge(Map<String, Object> map);
        MapBuilder rename(UnaryOperator<String> renameKeyFunction);
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
            ResultSetMetaData metaData = rs.getMetaData();
            HashMap<String, Object> resultSetMap = new HashMap<>();
            for (int index = 1; index <= metaData.getColumnCount(); index++)
                resultSetMap.put(metaData.getColumnName(index), rs.getObject(index));
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

    public static <T> Deque<T> push(Deque<T> deque, T value) {
        deque.push(value);
        return deque;
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
            T object = type.getDeclaredConstructor().newInstance();
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
