package net.thenextlvl.worlds.api.view;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GeneratorView {
    /**
     * Checks whether the given plugin has an associated generator.
     *
     * @param plugin the plugin to check for an associated generator
     * @return true if the plugin has a generator, otherwise false
     */
    
    @Contract(pure = true)
    boolean hasGenerator(Plugin plugin);

    /**
     * Checks if the specified plugin class has a declared method for generating custom chunk data.
     *
     * @param clazz the class of the plugin to check
     * @return true if the plugin class overrides the method for providing a ChunkGenerator, otherwise false
     */
    @Contract(pure = true)
    boolean hasChunkGenerator(Class<? extends Plugin> clazz);

    /**
     * Checks if the specified plugin class has a declared method for providing a default biome provider.
     *
     * @param clazz the class of the plugin to check
     * @return true if the plugin class overrides the method to provide a default biome provider, otherwise false
     */
    @Contract(pure = true)
    boolean hasBiomeProvider(Class<? extends Plugin> clazz);
}
