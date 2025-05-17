package net.thenextlvl.worlds.api.generator;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

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

    public static final GeneratorType SINGLE_BIOME = new GeneratorType(Key.key("minecraft", "fixed"));

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

    public Key presetName() {
        if (equals(DEBUG)) return Key.key("debug_all_block_states");
        if (equals(SINGLE_BIOME)) return Key.key("single_biome_surface");
        if (equals(NORMAL)) return Key.key("normal");
        return key();
    }

    public static Optional<GeneratorType> getByKey(Key key) {
        if (key.equals(AMPLIFIED.key())) return Optional.of(AMPLIFIED);
        if (key.equals(DEBUG.key())) return Optional.of(DEBUG);
        if (key.equals(FLAT.key())) return Optional.of(FLAT);
        if (key.equals(LARGE_BIOMES.key())) return Optional.of(LARGE_BIOMES);
        if (key.equals(NORMAL.key())) return Optional.of(NORMAL);
        if (key.equals(SINGLE_BIOME.key())) return Optional.of(SINGLE_BIOME);
        return Optional.empty();
    }
}
