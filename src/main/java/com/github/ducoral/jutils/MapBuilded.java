package com.github.ducoral.jutils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.github.ducoral.jutils.Core.*;

class MapBuilded implements MapBuilder {

    Map<String, Object> map;
    boolean ignore = false;

    MapBuilded(Map<String, Object> source) {
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
        return ignore ? ignoreKeyCase(map) : map;
    }
}
