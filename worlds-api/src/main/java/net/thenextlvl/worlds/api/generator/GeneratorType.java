package net.thenextlvl.worlds.api.generator;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record GeneratorType(Key key) implements Keyed {
    /**
     * Represents the "amplified" generator type.
     * <a href="https://minecraft.wiki/w/Amplified">Wiki</a>
     */
    public static final GeneratorType AMPLIFIED = new GeneratorType(Key.key("minecraft", "amplified"));

    /**
     * Represents the "debug" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#debug">Wiki</a>
     */
    public static final GeneratorType DEBUG = new GeneratorType(Key.key("minecraft", "debug"));

    /**
     * Represents the "flat" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#flat">Wiki</a>
     */
    public static final GeneratorType FLAT = new GeneratorType(Key.key("minecraft", "flat"));

    /**
     * Represents the "large_biomes" generator type.
     * <a href="https://minecraft.wiki/w/Large_Biomes">Wiki</a>
     */
    public static final GeneratorType LARGE_BIOMES = new GeneratorType(Key.key("minecraft", "large_biomes"));

    /**
     * Represents the "noise" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#noise">Wiki</a>
     */
    public static final GeneratorType NORMAL = new GeneratorType(Key.key("minecraft", "noise"));
}
