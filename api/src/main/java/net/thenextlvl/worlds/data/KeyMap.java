package net.thenextlvl.worlds.data;

import net.kyori.adventure.key.Key;

import java.util.HashMap;

public class KeyMap<V> extends HashMap<Key, V> {
    public KeyMap<V> add(Key key, V value) {
        put(key, value);
        return this;
    }
}
