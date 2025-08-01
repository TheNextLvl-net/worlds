package net.thenextlvl.worlds.api.exception;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The GeneratorException class is a custom exception that is thrown when a requested plugin as a generator
 * cannot be found, is disabled, or doesn't provide a chunk generator or biome provider.
 */
@NullMarked
public class GeneratorException extends RuntimeException {
    private final String plugin;
    private final @Nullable String id;

    /**
     * Constructs a new GeneratorException with the specified plugin name, generator ID, and error message.
     *
     * @param plugin  the name of the plugin associated with the generator
     * @param id      the unique identifier for the generator
     * @param message the detailed error message describing the reason for the exception
     */
    @ApiStatus.Internal
    public GeneratorException(String plugin, @Nullable String id, String message) {
        super(message);
        this.plugin = plugin;
        this.id = id;
    }

    /**
     * Retrieves the name of the plugin associated with the generator exception.
     *
     * @return a string representing the plugin name
     */
    @Contract(pure = true)
    public String getPlugin() {
        return plugin;
    }

    /**
     * Retrieves the unique identifier associated with the generator exception.
     *
     * @return a nullable string representing the ID of the generator, or null if no ID is provided
     */
    @Contract(pure = true)
    public @Nullable String getId() {
        return id;
    }
}
