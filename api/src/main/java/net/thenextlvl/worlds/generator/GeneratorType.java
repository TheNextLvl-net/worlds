package net.thenextlvl.worlds.generator;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.thenextlvl.worlds.preset.Preset;
import org.jetbrains.annotations.Contract;

import java.util.stream.Stream;

/**
 * Represents a world generator configuration.
 * <p>
 * Generator types are identified by the underlying dimension generator key.
 * Some generator types, such as {@link Flat} and {@link SingleBiome}, can
 * create configured variants with a preset or biome.
 *
 * @since 4.0.0
 */
public sealed interface GeneratorType extends Keyed permits GeneratorType.Flat, GeneratorType.SingleBiome, SimpleGeneratorType {
    /**
     * Represents the "amplified" generator type.
     * <a href="https://minecraft.wiki/w/Amplified">Wiki</a>
     *
     * @since 4.0.0
     */
    GeneratorType AMPLIFIED = new SimpleGeneratorType(Key.key("minecraft", "amplified"), "amplified");

    /**
     * Represents the "debug" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#debug">Wiki</a>
     *
     * @since 4.0.0
     */
    GeneratorType DEBUG = new SimpleGeneratorType(Key.key("minecraft", "debug"), "debug");

    /**
     * Represents the "flat" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#flat">Wiki</a>
     *
     * @since 4.0.0
     */
    Flat FLAT = new SimpleGeneratorType.Flat(Preset.CLASSIC_FLAT);

    /**
     * Represents the "large_biomes" generator type.
     * <a href="https://minecraft.wiki/w/Large_Biomes">Wiki</a>
     *
     * @since 4.0.0
     */
    GeneratorType LARGE_BIOMES = new SimpleGeneratorType(Key.key("minecraft", "large_biomes"), "large-biomes");

    /**
     * Represents the "noise" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#noise">Wiki</a>
     *
     * @since 4.0.0
     */
    GeneratorType NORMAL = new SimpleGeneratorType(Key.key("minecraft", "noise"), "normal");

    /**
     * Represents the "fixed" generator type.
     * <a href="https://minecraft.wiki/w/Dimension_definition#fixed">Wiki</a>
     *
     * @since 4.0.0
     */
    SingleBiome SINGLE_BIOME = new SimpleGeneratorType.SingleBiome(Key.key("minecraft", "plains"));

    /**
     * Returns the name of the generator type.
     *
     * @return the generator type name
     * @since 4.0.0
     */
    @Contract(pure = true)
    String name();

    /**
     * Returns whether this generator is the same type as another generator configuration.
     *
     * @param type the generator type to compare
     * @return {@code true} if both generators use the same type
     * @since 4.0.0
     */
    @Contract(pure = true)
    default boolean is(final GeneratorType type) {
        return key().equals(type.key());
    }

    /**
     * Returns the built-in generator types.
     *
     * @return a stream containing all generator types
     * @since 4.0.0
     */
    @Contract(pure = true)
    static Stream<GeneratorType> generatorTypes() {
        return Stream.of(AMPLIFIED, DEBUG, FLAT, LARGE_BIOMES, NORMAL, SINGLE_BIOME);
    }

    /**
     * The flat generator type.
     * Use {@link #with(Preset)} to configure the flat preset.
     *
     * @since 4.0.0
     */
    sealed interface Flat extends GeneratorType permits SimpleGeneratorType.Flat {
        /**
         * Creates a flat generator configuration with the specified preset.
         *
         * @param preset the flat-world preset
         * @return the configured generator type
         * @since 4.0.0
         */
        @Contract(value = "_ -> new", pure = true)
        Flat with(final Preset preset);

        /**
         * Gets the preset used by this flat generator type.
         *
         * @return the flat-world preset
         * @since 4.0.0
         */
        @Contract(pure = true)
        Preset preset();
    }

    /**
     * The single-biome generator type.
     * Use {@link #with(Key)} to configure the biome.
     *
     * @since 4.0.0
     */
    sealed interface SingleBiome extends GeneratorType permits SimpleGeneratorType.SingleBiome {
        /**
         * Creates a single-biome generator configuration with the specified biome.
         *
         * @param biome the biome key
         * @return the configured generator type
         * @since 4.0.0
         */
        @Contract(value = "_ -> new", pure = true)
        SingleBiome with(final Key biome);

        /**
         * Gets the biome used by this single-biome generator type.
         *
         * @return the biome key
         * @since 4.0.0
         */
        @Contract(pure = true)
        Key biome();
    }
}
