package net.thenextlvl.worlds.api.view;

import net.thenextlvl.worlds.api.event.WorldCloneEvent;
import net.thenextlvl.worlds.api.level.Level;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Interface representing a view for managing levels in a server environment.
 * It provides methods for reading, validating, and managing world directories and data.
 */
@NullMarked
public interface LevelView {
    /**
     * Retrieves the path to the backup folder.
     *
     * @return the {@link Path} representing the backup folder
     */
    Path getBackupFolder();

    /**
     * Retrieves the path to the world container directory.
     *
     * @return the {@link Path} representing the world container directory
     */
    Path getWorldContainer();

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
     * @deprecated use {@link #unloadAsync(World, boolean)}
     */
    @Deprecated(forRemoval = true, since = "3.2.0")
    default boolean unload(World world, boolean save) {
        return unloadAsync(world, save).join();
    }

    /**
     * Unloads the specified world from the server.
     *
     * @param world the world to be unloaded
     * @param save  whether changes to the world should be saved before unloading
     * @return A {@code CompletableFuture} completing with true if the world was successfully unloaded, otherwise false
     */
    CompletableFuture<Boolean> unloadAsync(World world, boolean save);

    /**
     * Saves the specified world, with an option to flush pending changes immediately.
     *
     * @param world the world to be saved
     * @param flush whether to flush pending changes to disk immediately
     * @deprecated use {@link #saveAsync(World, boolean)}
     */
    @Deprecated(forRemoval = true, since = "3.2.0")
    default void save(World world, boolean flush) {
        saveAsync(world, flush).join();
    }

    /**
     * Saves the specified world, with an option to flush pending changes immediately.
     *
     * @param world the world to be saved
     * @param flush whether to flush pending changes to disk immediately
     * @return A {@code CompletableFuture} that might complete exceptionally
     */
    CompletableFuture<Void> saveAsync(World world, boolean flush);

    /**
     * Saves the {@code level.dat} of the specified world to disk.
     *
     * @param world the world whose level data should be saved
     * @param async whether the save operation should be performed asynchronously
     * @deprecated use {@link #saveLevelDataAsync(World)}
     */
    @Deprecated(forRemoval = true, since = "3.2.0")
    default void saveLevelData(World world, boolean async) {
        var future = saveLevelDataAsync(world);
        if (!async) future.join();
    }

    /**
     * Saves the {@code level.dat} of the specified world to disk.
     *
     * @param world the world whose level data should be saved
     */
    CompletableFuture<Void> saveLevelDataAsync(World world);

    /**
     * Creates a backup for the given world and returns the size of the backup in bytes.
     *
     * @param world the world to back up
     * @return the size of the created backup in bytes
     * @throws IOException if an I/O error occurs while creating the backup
     */
    @SuppressWarnings("RedundantThrows")
    default long backup(World world) throws IOException {
        return backupAsync(world).join();
    }

    /**
     * Creates a backup for the given world and returns the size of the backup in bytes.
     * <p>
     * Completes with an {@link IOException} if an I/O error occurs while creating the backup
     *
     * @param world the world to back up
     * @return A {@code CompletableFuture} completing with the size of the created backup in bytes
     */
    CompletableFuture<Long> backupAsync(World world);

    /**
     * Clones the specified world with the possibility to modify its properties through a builder.
     * If a {@code full} clone is invoked, the entire world directory is duplicated,
     * except for specific files and folders: {@code advancements}, {@code datapacks},
     * {@code playerdata}, {@code stats}, {@code uid.dat}, and {@code session.lock}.
     * <p>
     * By default, if a name or key is not provided, they are automatically generated using the
     * pattern {@code OriginalName (#)}, and the key is a lowercased version of the generated name,
     * replacing spaces with underscores and removing invalid namespace characters.
     * <p>
     * Throws an {@code IllegalArgumentException} if the world name or key is already in use.
     * Throws an {@code IllegalStateException} if the target directory already exists.
     *
     * @param world   the world to be cloned
     * @param builder a consumer that modifies the {@link Level.Builder} properties of the cloned world
     * @param full    whether to fully clone including regions, entities..., or only the {@code level.dat}
     * @return an {@link Optional} containing the cloned world, or {@link Optional#empty()} if the cloning fails
     * @throws IllegalArgumentException if the world name or key are already in use
     * @throws IllegalStateException    if the target directory already exists
     * @throws IOException              if an I/O error occurs during the cloning process
     * @see WorldCloneEvent#isFullClone()
     * @deprecated use {@link #cloneAsync(World, Consumer, boolean)}
     */
    @SuppressWarnings("RedundantThrows")
    @Deprecated(forRemoval = true, since = "3.2.0")
    default Optional<World> clone(World world, Consumer<Level.Builder> builder, boolean full) throws IllegalArgumentException, IllegalStateException, IOException {
        return Optional.of(cloneAsync(world, builder, full).join());
    }

    /**
     * Clones the specified world with the possibility to modify its properties through a builder.
     * If a {@code full} clone is invoked, the entire world directory is duplicated,
     * except for specific files and folders: {@code advancements}, {@code datapacks},
     * {@code playerdata}, {@code stats}, {@code uid.dat}, and {@code session.lock}.
     * <p>
     * By default, if a name or key is not provided, they are automatically generated using the
     * pattern {@code OriginalName (#)}, and the key is a lowercased version of the generated name,
     * replacing spaces with underscores and removing invalid namespace characters.
     * <p>
     * Completes with an {@link IllegalArgumentException} if the world name or key is already in use.<br>
     * Completes with an {@link IllegalStateException} if the target directory already exists.<br>
     * Completes with an {@link IOException} if an I/O error occurs during the cloning process.
     *
     * @param world   the world to be cloned
     * @param builder a consumer that modifies the {@link Level.Builder} properties of the cloned world
     * @param full    whether to fully clone including regions, entities..., or only the {@code level.dat}
     * @return A {@code CompletableFuture} completing with the cloned world
     * @see WorldCloneEvent#isFullClone()
     */
    CompletableFuture<World> cloneAsync(World world, Consumer<Level.Builder> builder, boolean full);

    /**
     * Deletes the specified world from the server and disk.
     * The deletion can be executed immediately or scheduled for later, depending on the provided parameters.
     *
     * @param world    the world to be deleted
     * @param schedule if true, the deletion process will be scheduled for a later operation
     *                 (e.g., during server shutdown); if false, the deletion will be attempted immediately
     * @return a {@code DeletionResult} indicating the outcome of the deletion process.
     * @deprecated use {@link #deleteAsync(World, boolean)}
     */
    @Deprecated(forRemoval = true, since = "3.2.0")
    default DeletionResult delete(World world, boolean schedule) {
        return deleteAsync(world, schedule).join();
    }

    /**
     * Deletes the specified world from the server and disk.
     * The deletion can be executed immediately or scheduled for later, depending on the provided parameters.
     *
     * @param world    the world to be deleted
     * @param schedule if true, the deletion process will be scheduled for a later operation
     *                 (e.g., during server shutdown); if false, the deletion will be attempted immediately
     * @return A {@code CompletableFuture} completing with a {@code DeletionResult}
     * indicating the outcome of the deletion process.
     */
    CompletableFuture<DeletionResult> deleteAsync(World world, boolean schedule);

    /**
     * Cancels the deletion process for the specified world, if scheduled.
     *
     * @param world the world for which the scheduled deletion should be canceled
     * @return true if the scheduled deletion was successfully canceled, false if no deletion was scheduled
     */
    boolean cancelScheduledDeletion(World world);

    /**
     * Checks whether a deletion process is scheduled for the specified world.
     *
     * @param world the world to check for a scheduled deletion
     * @return true if a deletion process is scheduled for the world, otherwise false
     */
    boolean isDeletionScheduled(World world);

    /**
     * Regenerates the specified world, either immediately or scheduled, based on the provided parameters.
     *
     * @param world    the world to be regenerated
     * @param schedule if true, the regeneration will be scheduled for later execution;
     *                 if false, the regeneration will be attempted immediately
     * @return a {@code DeletionResult} indicating the outcome of the regeneration process.
     * @deprecated use {@link #regenerateAsync(World, boolean)}
     */
    @Deprecated(forRemoval = true, since = "3.2.0")
    default DeletionResult regenerate(World world, boolean schedule) {
        return regenerateAsync(world, schedule).join();
    }

    /**
     * Regenerates the specified world, either immediately or scheduled, based on the provided parameters.
     *
     * @param world    the world to be regenerated
     * @param schedule if true, the regeneration will be scheduled for later execution;
     *                 if false, the regeneration will be attempted immediately
     * @return a {@code CompletableFuture} completing with a
     * {@code DeletionResult} indicating the outcome of the regeneration process.
     */
    CompletableFuture<DeletionResult> regenerateAsync(World world, boolean schedule);

    /**
     * Cancels the regeneration process for the specified world, if scheduled.
     *
     * @param world the world for which the scheduled regeneration should be canceled
     * @return true if the scheduled regeneration was successfully canceled, false if no regeneration was scheduled
     */
    boolean cancelScheduledRegeneration(World world);

    /**
     * Checks whether a regeneration process is scheduled for the specified world.
     *
     * @param world the world to check for a scheduled regeneration
     * @return true if a regeneration process is scheduled for the world, otherwise false
     */
    boolean isRegenerationScheduled(World world);

    /**
     * Represents the possible outcomes of a world deletion process.
     */
    enum DeletionResult {
        /**
         * Indicates that the deletion process was completed successfully.
         */
        SUCCESS,
        /**
         * Indicates that the deletion process has been scheduled to occur during the server shutdown process.
         */
        SCHEDULED,
        /**
         * Indicates that the deletion process requires scheduling due to certain constraints
         * that prevent immediate execution, like the {@code minecraft:overworld}
         */
        REQUIRES_SCHEDULING,
        /**
         * Indicates that the deletion process failed due to the inability to unload the world.
         */
        UNLOAD_FAILED,
        /**
         * Indicates that the deletion process failed due to an unspecified error or issue
         * that prevented the operation from completing successfully.
         */
        FAILED;

        /**
         * Determines if the current deletion result represents a successful operation.
         * A deletion result is considered successful if it is either {@code SUCCESS}
         * or {@code SCHEDULED}.
         *
         * @return true if the result is either {@code SUCCESS} or {@code SCHEDULED}, otherwise false
         */
        public boolean isSuccess() {
            return this == SUCCESS || this == SCHEDULED;
        }
    }
}
