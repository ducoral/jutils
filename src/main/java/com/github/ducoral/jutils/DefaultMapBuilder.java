package com.github.ducoral.jutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.github.ducoral.jutils.Core.*;

class DefaultMapBuilder implements MapBuilder {

    Map<String, Object> map;
    boolean ignore = false;

    DefaultMapBuilder(Map<String, Object> source) {
        map = new HashMap<>(source);
    }

    public Core.MapBuilder merge(Map<String, Object> map) {
        this.map.putAll(map);
        return this;
    }

    public Core.MapBuilder rename(Function<String, String> renameKeyFunction) {
        map = new HashMap<String, Object>() {{
            for (String key : map.keySet())
                put(renameKeyFunction.apply(key), map.get(key));
        }};
        return this;
    }

    public Core.MapBuilder pair(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public Core.MapBuilder ignore() {
        ignore = true;
        return this;
    }

    public Map<String, Object> done() {
        return ignore ? new IgnoreCaseHashMap(map) : map;
    }

    private static class IgnoreCaseHashMap implements Map<String, Object> {

        private final Map<String, Object> map = new HashMap<>();

        IgnoreCaseHashMap(Map<String, Object> source) {
            putAll(source);
        }

        public int size() {
            return map.size();
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public boolean containsKey(Object key) {
            return map.containsKey(lower(key));
        }

        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        public Object get(Object key) {
            return map.get(lower(key));
        }

        public Object put(String key, Object value) {
            return map.put(lower(key), value);
        }

        public Object remove(Object key) {
            return map.remove(lower(key));
        }

        public void putAll(Map<? extends String, ?> source) {
            for (Entry<? extends String, ?> entry : source.entrySet())
                map.put(lower(entry.getKey()), entry.getValue());
        }

        public void clear() {
            map.clear();
        }

        public Set<String> keySet() {
            return map.keySet();
        }

        public Collection<Object> values() {
            return map.values();
        }

        public Set<Entry<String, Object>> entrySet() {
            return map.entrySet();
        }
    }
}
