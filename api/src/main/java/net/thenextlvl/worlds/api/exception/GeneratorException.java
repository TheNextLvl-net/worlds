package net.thenextlvl.worlds.api.exception;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The GeneratorException class is a custom exception that is thrown when a requested plugin as a generator
 * cannot be found, is disabled, or doesn't provide a chunk generator or biome provider.
 */
@Getter
@NullMarked
public class GeneratorException extends RuntimeException {
    private final String plugin;
    private final @Nullable String id;

    public GeneratorException(String plugin, @Nullable String id, String message) {
        super(message);
        this.plugin = plugin;
        this.id = id;
    }
}
