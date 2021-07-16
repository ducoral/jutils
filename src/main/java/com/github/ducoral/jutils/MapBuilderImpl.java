package com.github.ducoral.jutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.UnaryOperator;

import static com.github.ducoral.jutils.Core.MapBuilder;
import static com.github.ducoral.jutils.Core.lower;

class MapBuilderImpl implements MapBuilder {

    Map<String, Object> map;
    boolean ignore = false;

    MapBuilderImpl(Map<String, Object> source) {
        map = new HashMap<>(source);
    }

    public MapBuilder merge(Map<String, Object> map) {
        this.map.putAll(map);
        return this;
    }

    public MapBuilder rename(UnaryOperator<String> renameKeyFunction) {
        Map<String, Object> newMap = new HashMap<>();
        for (Entry<String, Object> entry : map.entrySet())
            newMap.put(renameKeyFunction.apply(entry.getKey()), map.get(entry.getKey()));
        map = newMap;
        return this;
    }

    public MapBuilder pair(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder ignore() {
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
