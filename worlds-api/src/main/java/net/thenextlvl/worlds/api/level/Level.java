package net.thenextlvl.worlds.api.level;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@NullMarked
public interface Level extends Keyed {
    /**
     * Retrieves the file path associated with this level.
     *
     * @return the file path as a {@code Path} object
     */
    Path getDirectory();

    /**
     * Retrieves the name of this level.
     *
     * @return the level name as a {@code String}
     */
    String getName();

    /**
     * Retrieves an {@link Optional} containing a {@link Preset} associated with the level, if available.
     *
     * @return an {@code Optional} containing the {@code Preset} if one is present, or {@link Optional#empty()}
     * if no preset is associated with the level
     */
    Optional<Preset> getPreset();

    /**
     * Retrieves an {@link Optional} containing a {@link BiomeProvider} associated with the level, if available.
     *
     * @return an {@code Optional} containing the {@code BiomeProvider} if one is present, or {@link Optional#empty()}
     * if no biome provider is associated with the level
     */
    Optional<BiomeProvider> getBiomeProvider();

    /**
     * Retrieves an {@link Optional} containing a {@link ChunkGenerator} associated with the level, if available.
     *
     * @return an {@code Optional} containing the {@code ChunkGenerator} if one is present, or {@link Optional#empty()}
     * if no chunk generator is associated with the level
     */
    Optional<ChunkGenerator> getChunkGenerator();

    /**
     * Retrieves an {@link Optional} containing a {@link Generator} associated with the level, if available.
     *
     * @return an {@code Optional} containing the {@code Generator} if one is present, or {@link Optional#empty()}
     * if no generator is associated with the level
     */
    Optional<Generator> getGenerator();

    /**
     * Determines whether the level is in hardcore mode.
     *
     * @return true if the level is set to hardcore mode, false otherwise
     */
    boolean isHardcore();

    /**
     * Determines whether the level contains structures.
     *
     * @return true if structures are present in the level, false otherwise
     */
    boolean hasStructures();

    /**
     * Determines whether the level has a bonus chest enabled.
     *
     * @return true if the bonus chest is enabled for this level, false otherwise
     */
    boolean hasBonusChest();

    /**
     * Retrieves the spawn chunk radius for the level.
     *
     * @return an integer representing the spawn chunk radius
     */
    int getSpawnChunkRadius();

    /**
     * Retrieves the seed value associated with this Level.
     *
     * @return the seed value as a {@code long}
     */
    long getSeed();

    @ApiStatus.Experimental
    GeneratorType getGeneratorType();

    @ApiStatus.Experimental
    LevelStem getLevelStem();

    @ApiStatus.Internal
    TriState isEnabled();

    @ApiStatus.Internal
    boolean isWorldKnown();

    /**
     * Converts the current Level instance into a Builder, allowing modifications
     * to its configuration before creating or modifying a new Level instance.
     *
     * @return a Builder instance initialized with the current Level's configuration
     */
    Builder toBuilder();

    /**
     * Creates and initializes a new {@link World} based on the current configuration of the {@link Level} instance.
     * This method will return an {@code Optional} containing the created {@code World} if successful,
     * or an empty {@code Optional} if the creation process fails.
     *
     * @return an {@code Optional} containing the created {@code World}, or {@link Optional#empty()} otherwise
     * @deprecated use {@link #createAsync()}
     */
    @Deprecated(forRemoval = true, since = "3.2.0")
    default Optional<World> create() {
        try {
            return Optional.of(createAsync().get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates and initializes a new {@link World} based on the current configuration of the {@link Level} instance.
     * <p>
     * Completes exceptionally if:
     * 
     *
     * @return a {@code CompletableFuture} completing with the created {@link World}
     */
    CompletableFuture<World> createAsync();

    interface Builder {
        /**
         * Retrieves the directory path associated with the builder.
         *
         * @return the {@link Path} representing the directory, or null if no directory is set
         */
        Path directory();

        /**
         * Sets the directory path associated with the builder.
         *
         * @param directory the {@link Path} representing the directory to set
         * @return the builder instance for chaining method calls
         */
        Builder directory(Path directory);

        /**
         * Retrieves the key associated with the builder, if one has been set.
         *
         * @return the {@link Key} instance associated with the builder, or null if no key is set
         */
        @Nullable
        Key key();

        /**
         * Sets the key associated with the builder.
         *
         * @param key the {@link Key} instance to associate with the builder, or null to unset the key
         * @return the builder instance for chaining method calls
         */
        Builder key(@Nullable Key key);

        /**
         * Retrieves the name associated with the builder.
         *
         * @return the name as a {@link String}, or null if no name is set
         */
        @Nullable
        String name();

        /**
         * Sets the name associated with the builder.
         *
         * @param name the name to associate with the builder, or null to unset the name
         * @return the builder instance for chaining method calls
         */
        Builder name(@Nullable String name);

        /**
         * Retrieves the {@link BiomeProvider} associated with the builder.
         * The biome provider is responsible for determining which biomes will be
         * used during world generation.
         *
         * @return the {@link BiomeProvider} instance, or null if no biome provider is set
         */
        @Nullable
        BiomeProvider biomeProvider();

        /**
         * Sets the {@link BiomeProvider} instance for the builder.
         * The biome provider determines how biomes are assigned and distributed in the generated world.
         *
         * @param provider the {@link BiomeProvider} to set, or null to remove any previously set biome provider
         * @return the builder instance for chaining method calls
         */
        Builder biomeProvider(@Nullable BiomeProvider provider);

        /**
         * Retrieves the {@link ChunkGenerator} instance configured for the builder.
         * The chunk generator defines how world generation is handled, including
         * terrain creation and feature placement.
         *
         * @return the {@link ChunkGenerator} instance associated with the builder,
         * or null if no chunk generator has been set.
         */
        @Nullable
        ChunkGenerator chunkGenerator();

        /**
         * Sets the {@link ChunkGenerator} instance to be used in the builder.
         * The chunk generator defines how the world will be generated, including terrain and features.
         *
         * @param generator the {@link ChunkGenerator} instance to set, or null to remove the chunk generator.
         * @return the builder instance for chaining method calls
         */
        Builder chunkGenerator(@Nullable ChunkGenerator generator);

        /**
         * Retrieves the generator configuration if one is defined.
         * The generator defines specific behaviors or features for customizing world generation.
         *
         * @return the {@link Generator} instance associated with the builder, or null if no generator is set
         */
        @Nullable
        Generator generator();

        /**
         * Sets the generator configuration for the builder.
         *
         * @param generator the {@link Generator} instance to set, or null to remove the generator configuration
         * @return the builder instance for chaining method calls
         */
        Builder generator(@Nullable Generator generator);

        /**
         * Retrieves the preset configuration associated with the builder.
         * The preset determines predefined settings for world generation.
         *
         * @return the {@link Preset} applied to the builder, or null if no preset is set
         */
        @Nullable
        Preset preset();

        /**
         * Sets the preset configuration for the builder.
         * The preset determines pre-defined settings that can be applied to the world generation.
         *
         * @param preset the {@link Preset} to apply to the builder, or null to leave the preset undefined
         * @return the builder instance for chaining method calls
         */
        Builder preset(@Nullable Preset preset);

        /**
         * Retrieves whether the world is configured to be in hardcore mode.
         *
         * @return a {@link Boolean} indicating whether the world is in hardcore mode,
         * or null if the value is not explicitly set and the server default should be used.
         */
        @Nullable
        Boolean hardcore();

        /**
         * Sets whether the world will be in hardcore mode.
         * If the value is null, the {@link Server#isHardcore() configured value} will be used.
         *
         * @param hardcore a {@link Boolean} indicating whether the world should be in hardcore mode,
         *                 or null to use the server default
         * @return the builder instance for chaining method calls
         */
        Builder hardcore(@Nullable Boolean hardcore);

        /**
         * Retrieves whether structures should be generated in the world.
         *
         * @return a {@link Boolean} indicating whether structures should be generated,
         * or null if the server default should be used
         */
        @Nullable
        Boolean structures();

        /**
         * Sets whether structures should be generated in the world.
         * If the value is null, the {@link Server#getGenerateStructures() configured value} will be used.
         *
         * @param structures a {@link Boolean} indicating whether structures should
         *                   be generated, or null to use the server default
         * @return the builder instance for chaining method calls
         */
        Builder structures(@Nullable Boolean structures);

        /**
         * Retrieves whether a bonus chest should be generated in the world.
         *
         * @return a {@link Boolean} indicating whether a bonus chest should be generated.
         */
        @Nullable
        Boolean bonusChest();

        /**
         * Sets whether a bonus chest should be generated in the world.
         * A bonus chest is a starting chest containing basic resources to help players begin their journey.
         *
         * @param bonusChest a {@link Boolean} specifying whether a bonus chest
         *                   should be generated, or null to leave the value unset
         * @return the builder instance for chaining method calls
         */
        Builder bonusChest(@Nullable Boolean bonusChest);

        /**
         * Retrieves the configured radius of chunks around the spawn point that should remain loaded.
         *
         * @return an {@link Integer} representing the radius of spawn chunks to keep loaded,
         * or null if the default value is to be used.
         */
        @Nullable
        Integer spawnChunkRadius();

        /**
         * Sets the radius of chunks around the spawn point that should remain loaded.
         * If the value is null, the {@link Server#getSpawnRadius() configured value} will be used.
         *
         * @param radius an {@link Integer} representing the radius of chunks to keep loaded,
         *               or null to use the server default
         * @return the builder instance for chaining method calls
         */
        Builder spawnChunkRadius(@Nullable Integer radius);

        /**
         * Retrieves the world seed used for generation, if available.
         *
         * @return the seed as a {@link Long}, or null if no seed is set
         */
        @Nullable
        Long seed();

        /**
         * Sets the world seed.
         *
         * @param seed the seed to use for world generation, or null to generate a random seed
         * @return this builder instance for chaining method calls
         */
        Builder seed(@Nullable Long seed);

        /**
         * Constructs a new Level instance based on the current state of the builder.
         *
         * @return the constructed Level instance
         */
        Level build();

        @Nullable
        @ApiStatus.Experimental
        LevelStem levelStem();

        @ApiStatus.Experimental
        Builder levelStem(@Nullable LevelStem type);

        @Nullable
        @ApiStatus.Experimental
        GeneratorType generatorType();

        @ApiStatus.Experimental
        Builder generatorType(@Nullable GeneratorType type);

        @Nullable
        @ApiStatus.Internal
        Boolean worldKnown();

        @ApiStatus.Internal
        Builder worldKnown(@Nullable Boolean worldKnown);

        @ApiStatus.Internal
        TriState enabled();

        @ApiStatus.Internal
        Builder enabled(TriState enabled);
    }
}
