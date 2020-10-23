package com.github.ducoral.jutils;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;

import static com.github.ducoral.jutils.Core.*;

public final class XML {

    public static class Element {
        public final String name;
        public final String value;
        public final Map<String, String> attributes;
        public final List<Element> children;

        private Element(String name, String value) {
            this.name = name;
            this.value = value;
            attributes = new HashMap<>();
            children = new ArrayList<>();
        }

        private Element(final Node node) {
            name = node.getNodeName();
            value = node.hasChildNodes() ? safe(node.getFirstChild().getNodeValue()) : "";
            attributes = new HashMap<String, String>() {{
                NamedNodeMap map = node.getAttributes();
                for (int index = 0; index < map.getLength(); index++)
                    put(map.item(index).getNodeName(), map.item(index).getNodeValue());
            }};
            children = new ArrayList<Element>() {{
                NodeList list = node.getChildNodes();
                for (int index = 0; index < list.getLength(); index++)
                    if (list.item(index).getNodeType() == Node.ELEMENT_NODE)
                        add(new Element(list.item(index)));
            }};
        }

        public boolean isEmpty() {
            return value.isEmpty() && children.isEmpty();
        }

        public boolean hasChildren() {
            return !children.isEmpty();
        }

        private String toString(int indent, int level, Element element) {
            String spaces = str(level * indent, ' ');
            final StringBuilder formatted = new StringBuilder(spaces).append('<').append(element.name);
            element.attributes.forEach((name, value) ->
                    formatted.append(' ').append(name).append("=\"").append(value).append("\""));
            if (element.isEmpty())
                formatted.append('/');
            formatted.append('>').append(element.value);
            element.children.forEach(child -> formatted.append('\n').append(toString(indent,level + 1, child)));
            if (element.hasChildren())
                formatted.append('\n').append(spaces);
            if (!element.isEmpty())
                formatted.append("</").append(element.name).append('>');
            return formatted.toString();
        }

        public String toString(int indent) {
            return toString(indent,0,this);
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder("<").append(name);
            attributes.forEach((key, value) -> str.append(' ').append(keyValue(key, value)));
            if (isEmpty())
                str.append("/>");
            else {
                str.append('>').append(value);
                for (Element child : children)
                    str.append(child);
                str.append("</").append(name).append(">");
            }
            return str.toString();
        }

        public String attribute(String name) {
            return attributes.getOrDefault(name, "");
        }
    }

    private static String keyValue(String key, String value) {
        return key + "=\"" + value + "\"";
    }

    public static Element root(Document document) {
        return new Element(document.getDocumentElement());
    }

    public static Element root(InputStream xml) {
        try {
            return new Element(builder().parse(xml).getDocumentElement());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Element root(InputSource xml) {
        try {
            return new Element(builder().parse(xml).getDocumentElement());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Element root(File xml) {
        try {
            return new Element(builder().parse(xml).getDocumentElement());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Element root(String xml) {
        try {
            return new Element(builder().parse(new InputSource(new StringReader(xml))).getDocumentElement());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Element element(String name, Object... items) {
        String value = "";
        for (Object item : items)
            if (item instanceof String)
                value = (String) item;
        Element element = new Element(name, value);
        for (Object item : items)
            if (item instanceof Attr)
                ((Attr) item).set(element.attributes);
            else if (item instanceof Element)
                element.children.add((Element) item);
        return element;
    }

    public static Object attribute(String name, String value) {
        return new Attr(name, value);
    }

    public static Element accept(Iterator<Element> iterator, String... tags) {
        if (iterator.hasNext())
            return accept(iterator.next(), tags);
        else
            throw new Oops("TAG faltante: %s", tags);
    }

    public static Element accept(XML.Element element, String... tags) {
        if (isOneOf(element.name, tags))
            return element;
        else
            throw new Oops("Era esperada a TAG %s porém encontrou %s", element.name, tags);
    }

    private static DocumentBuilder builder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static class Attr {
        final String name;
        final String value;
        Attr(String name, String value) {
            this.name = name;
            this.value = value;
        }
        void set(Map<String, String> map) {
            map.put(name, value);
        }
    }

    private XML() {
    }
}
