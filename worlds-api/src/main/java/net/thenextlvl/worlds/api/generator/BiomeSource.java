package net.thenextlvl.worlds.api.generator;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

public record BiomeSource(Key key) implements Keyed {
    /**
     * Represents the "checkerboard" biome source.
     * <p>
     * This biome source generates a unique checkerboard-like pattern of biomes, creating a grid-like world layout.
     * <a href="https://minecraft.wiki/w/Dimension_definition#checkerboard">Wiki</a>
     */
    public static final GeneratorType CHECKERBOARD = new GeneratorType(Key.key("minecraft", "checkerboard"));

    /**
     * Represents the "fixed" biome source.
     * <p>
     * This preset generates a world consisting of only one biome.
     * <a href="https://minecraft.wiki/w/Dimension_definition#fixed">Wiki</a>
     */
    public static final GeneratorType SINGLE_BIOME = new GeneratorType(Key.key("minecraft", "fixed"));
}
