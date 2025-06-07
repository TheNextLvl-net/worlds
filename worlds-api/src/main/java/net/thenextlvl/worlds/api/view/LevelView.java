package net.thenextlvl.worlds.api.view;

import net.thenextlvl.worlds.api.level.Level;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * Interface representing a view for managing levels in a server environment. 
 * It provides methods for reading, validating, and managing world directories and data.
 */
@NullMarked
public interface LevelView {
    /**
     * Reads a level from the specified directory path.
     *
     * @param directory the directory containing the level data to be read
     * @return an {@code Optional} containing the {@code Level.Builder} if the directory represents a valid level,
     * or {@link Optional#empty()} if the directory is invalid
     */
    Optional<Level.Builder> read(Path directory);

    /**
     * Retrieves the plugin associated with the world generator for the given world, if it exists.
     *
     * @param world the world whose generator plugin is to be retrieved
     * @return an {@code Optional} containing the associated {@code JavaPlugin}, or {@link Optional#empty()}
     * if the world does not have a generator or if the generator is not associated with a plugin
     */
    Optional<JavaPlugin> getGenerator(World world);

    /**
     * Returns an unmodifiable set of paths representing the directories of all levels
     * available in the server's {@link Server#getWorldContainer() world container}.
     * These directories are determined to be valid levels using specific validation criteria.
     *
     * @return an unmodifiable set of {@link Path} objects representing valid level directories,
     * or an empty set if no valid levels are found or if an error occurs while accessing the filesystem.
     */
    @Unmodifiable
    Set<Path> listLevels();

    /**
     * Determines if a level located at the specified path can be loaded.
     * A level can be loaded if there is no other world loaded with the same path.
     *
     * @param level the path to the level directory to be checked
     * @return true if the level can be loaded, otherwise false
     */
    boolean canLoad(Path level);

    /**
     * Determines whether the specified directory contains an End dimension folder.
     *
     * @param level the path to the directory to be checked
     * @return true if the level directory contains a {@code DIM1} folder, otherwise false
     */
    boolean hasEndDimension(Path level);

    /**
     * Determines whether the specified directory contains a Nether dimension folder.
     *
     * @param level the path to the directory to be checked
     * @return true if the level directory contains a {@code DIM-1} folder, otherwise false
     */
    boolean hasNetherDimension(Path level);

    /**
     * Determines whether the specified directory represents a valid world folder.
     *
     * @param path the path to the directory being checked
     * @return {@code true} if the directory contains a {@code level.dat}
     * or {@code level.dat_old}, otherwise {@code false}
     */
    boolean isLevel(Path path);

    /**
     * Unloads the specified world from the server.
     *
     * @param world the world to be unloaded
     * @param save  whether changes to the world should be saved before unloading
     * @return true if the world was successfully unloaded, otherwise false
     */
    boolean unload(World world, boolean save);

    /**
     * Saves the specified world, with an option to flush pending changes immediately.
     *
     * @param world the world to be saved
     * @param flush whether to flush pending changes to disk immediately
     */
    void save(World world, boolean flush);

    /**
     * Saves the {@code level.dat} of the specified world to disk.
     *
     * @param world the world whose level data should be saved
     * @param async whether the save operation should be performed asynchronously
     */
    void saveLevelData(World world, boolean async);
}
