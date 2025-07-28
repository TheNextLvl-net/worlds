package net.thenextlvl.worlds.api;

import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.link.LinkProvider;
import net.thenextlvl.worlds.api.link.LinkTree;
import net.thenextlvl.worlds.api.view.GeneratorView;
import net.thenextlvl.worlds.api.view.LevelView;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

/**
 * The WorldsProvider is the main API interface that enables interaction with world management
 * functionalities such as chunk generation, level handling, and world grouping.
 * It provides access to various tools and services
 * that facilitate operations on Minecraft world data and configurations.
 */
@NullMarked
public interface WorldsProvider extends Plugin {
    /**
     * Provides access to the {@link GeneratorView} instance, which allows querying and interacting
     * with the generator-related functionality of plugins, such as verifying the presence
     * of chunk generators or biome providers.
     *
     * @return the {@link GeneratorView} instance used for managing and retrieving generator information
     */
    GeneratorView generatorView();

    /**
     * Creates a {@link Level.Builder} instance to configure and build a level from the specified directory.
     * The given level path will be resolved from the {@link Server#getWorldContainer() world container}.
     *
     * @param level the path representing the level to be built
     * @return a {@link Level.Builder} for configuring and creating the specified level
     */
    Level.Builder levelBuilder(Path level);

    /**
     * Creates a {@link Level.Builder} instance as a copy of the specified {@link World}.
     * This method allows configuration and building of a level based on the given world's properties.
     *
     * @param world the world to create a builder copy from
     * @return a {@link Level.Builder} instance for configuring and modifying the copied level
     */
    Level.Builder levelBuilder(World world);

    /**
     * Provides access to the {@link LevelView} functionality, which includes operations
     * for managing Minecraft world levels, such as saving, unloading, and retrieving level data.
     *
     * @return the {@link LevelView} instance used to interact with world level operations
     */
    LevelView levelView();

    /**
     * Retrieves the {@link LinkProvider} responsible for managing and retrieving associations
     * between worlds and their respective {@link LinkTree} structures.
     * This provider facilitates the creation, querying,
     * and management of links between worlds and their portal relationships.
     *
     * @return the {@link LinkProvider} instance
     */
    LinkProvider linkProvider();

    /**
     * Retrieves the {@link net.thenextlvl.perworlds.GroupProvider} instance responsible for managing and interacting with world groups.
     * A group provider facilitates the creation, retrieval, and modification of world groups.
     *
     * @return the {@link net.thenextlvl.perworlds.GroupProvider} instance, or {@code null} if unsupported (e.g., in environments like Folia)
     * @deprecated Worlds does no longer provide PerWorlds anymore.
     */
    @Deprecated(forRemoval = true, since = "3.3.0")
    default net.thenextlvl.perworlds.@Nullable GroupProvider groupProvider() {
        return org.bukkit.Bukkit.getServicesManager().load(net.thenextlvl.perworlds.GroupProvider.class);
    }
}
