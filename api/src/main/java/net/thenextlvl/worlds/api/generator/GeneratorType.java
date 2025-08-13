package net.thenextlvl.worlds.api.generator;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * @since 3.0.0
 */
@NullMarked
@ApiStatus.Experimental
public record GeneratorType(Key key, String name) implements Keyed {
    /**
     * Represents the "amplified" generator type.
     * <a href="https://minecraft.wiki/w/Amplified">Wiki</a>
     */
    public static final GeneratorType AMPLIFIED = new GeneratorType(Key.key("minecraft", "amplified"), "amplified");

    /**
     * Represents the "debug" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#debug">Wiki</a>
     */
    public static final GeneratorType DEBUG = new GeneratorType(Key.key("minecraft", "debug"), "debug");

    /**
     * Represents the "flat" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#flat">Wiki</a>
     */
    public static final GeneratorType FLAT = new GeneratorType(Key.key("minecraft", "flat"), "flat");

    /**
     * Represents the "large_biomes" generator type.
     * <a href="https://minecraft.wiki/w/Large_Biomes">Wiki</a>
     */
    public static final GeneratorType LARGE_BIOMES = new GeneratorType(Key.key("minecraft", "large_biomes"), "large-biomes");

    /**
     * Represents the "noise" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#noise">Wiki</a>
     */
    public static final GeneratorType NORMAL = new GeneratorType(Key.key("minecraft", "noise"), "normal");

    /**
     * Represents the "noise" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#fixed">Wiki</a>
     */
    public static final GeneratorType SINGLE_BIOME = new GeneratorType(Key.key("minecraft", "fixed"), "single-biome");

    @ApiStatus.Internal
    @Contract(pure = true)
    public Key presetName() {
        if (equals(DEBUG)) return Key.key("debug_all_block_states");
        if (equals(SINGLE_BIOME)) return Key.key("single_biome_surface");
        if (equals(NORMAL)) return Key.key("normal");
        return key();
    }

    /**
     * @since 3.4.0
     */
    @Contract(pure = true)
    public static Optional<GeneratorType> getByName(String name) {
        if (name.equals(AMPLIFIED.name())) return Optional.of(AMPLIFIED);
        if (name.equals(DEBUG.name())) return Optional.of(DEBUG);
        if (name.equals(FLAT.name())) return Optional.of(FLAT);
        if (name.equals(LARGE_BIOMES.name())) return Optional.of(LARGE_BIOMES);
        if (name.equals(NORMAL.name())) return Optional.of(NORMAL);
        if (name.equals(SINGLE_BIOME.name())) return Optional.of(SINGLE_BIOME);
        return Optional.empty();
    }
}
