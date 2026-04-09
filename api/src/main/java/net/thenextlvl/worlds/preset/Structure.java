package net.thenextlvl.worlds.preset;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.Contract;

/**
 * Represents a structure override used in a Superflat world preset.
 *
 * @param key the namespaced key identifying the structure
 * @since 4.0.0
 */
public record Structure(Key key) implements Keyed {
    /**
     * Creates a structure from a namespaced key string.
     *
     * @param string the namespaced key string
     * @since 4.0.0
     */
    public Structure(@KeyPattern final String string) {
        this(Key.key(string));
    }

    @Override
    public String toString() {
        return key().asString();
    }

    /**
     * Creates a structure from a namespaced key string.
     *
     * @param structure the namespaced key string
     * @return a new structure instance
     * @since 4.0.0
     */
    @Contract(value = "_ -> new", pure = true)
    public static Structure literal(@KeyPattern final String structure) {
        return new Structure(Key.key(structure));
    }
}
