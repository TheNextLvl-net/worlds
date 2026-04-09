package net.thenextlvl.worlds.preset;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.Contract;

/**
 * Represents a biome used in a Superflat world preset.
 *
 * @param key the namespaced key identifying the biome
 * @since 4.0.0
 */
public record Biome(Key key) implements Keyed {
    /**
     * Creates a biome from a Bukkit {@link org.bukkit.block.Biome}.
     *
     * @param biome the Bukkit biome
     * @since 4.0.0
     */
    public Biome(final org.bukkit.block.Biome biome) {
        this(biome.key());
    }

    @Override
    public String toString() {
        return key().asString();
    }

    /**
     * Creates a biome from a namespaced key string.
     *
     * @param string the namespaced key string
     * @return a new biome instance
     * @since 4.0.0
     */
    @Contract(value = "_ -> new", pure = true)
    public static Biome literal(@KeyPattern final String string) {
        return new Biome(Key.key(string));
    }
}
