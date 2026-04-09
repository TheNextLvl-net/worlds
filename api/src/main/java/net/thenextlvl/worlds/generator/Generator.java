package net.thenextlvl.worlds.generator;

import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * This interface represents a generator that interacts with the Minecraft world generation system.
 * It encapsulates a plugin responsible for providing the generator and an optional identifier
 * for specifying a unique generator configuration.
 * <p>
 * The {@code Generator} offers methods to serialize its state, retrieve chunk generators,
 * and obtain biome providers for specific world names.
 *
 * @since 4.0.0
 */
public sealed interface Generator permits SimpleGenerator {
    /**
     * Returns the plugin responsible for providing this generator.
     *
     * @return the plugin associated with this generator
     * @since 4.0.0
     */
    @Contract(pure = true)
    Plugin getPlugin();

    /**
     * Returns the optional identifier used to specify a unique generator configuration
     * within the plugin.
     *
     * @return an optional containing the generator id, or empty if none is specified
     * @since 4.0.0
     */
    @Contract(pure = true)
    Optional<String> getId();

    /**
     * Retrieves the {@link ChunkGenerator} provided by the plugin for the given world name.
     *
     * @param worldName the name of the world to get the chunk generator for
     * @return an optional containing the chunk generator, or empty if none is provided
     * @since 4.0.0
     */
    @Contract(pure = true)
    Optional<ChunkGenerator> generator(final String worldName);

    /**
     * Retrieves the {@link BiomeProvider} provided by the plugin for the given world name.
     *
     * @param worldName the name of the world to get the biome provider for
     * @return an optional containing the biome provider, or empty if none is provided
     * @since 4.0.0
     */
    @Contract(pure = true)
    Optional<BiomeProvider> biomeProvider(final String worldName);

    /**
     * Serializes this generator to a string representation.
     * <p>
     * The format is {@code pluginName:id} if an id is present, or just {@code pluginName} otherwise.
     *
     * @return the string representation of this generator
     * @since 4.0.0
     */
    @Contract(pure = true)
    String asString();

    /**
     * Creates a new generator for the given plugin and optional configuration id.
     *
     * @param plugin the plugin providing the generator
     * @param id     the optional generator configuration id, or null for the default
     * @return a new generator instance
     * @since 4.0.0
     */
    @Contract(value = "_, _ -> new", pure = true)
    static Generator of(final Plugin plugin, @Nullable final String id) {
        return new SimpleGenerator(plugin, id);
    }

    /**
     * Parses a generator from its string representation.
     * <p>
     * The expected format is {@code pluginName} or {@code pluginName:id}.
     *
     * @param string the string to parse
     * @return a new generator instance
     * @throws GeneratorException if the string cannot be resolved to a valid generator
     * @since 4.0.0
     */
    @Contract(value = "_ -> new", pure = true)
    static Generator fromString(final String string) throws GeneratorException {
        return SimpleGenerator.of(string);
    }
}
