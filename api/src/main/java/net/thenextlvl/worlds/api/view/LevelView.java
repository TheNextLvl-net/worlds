package net.thenextlvl.worlds.api.view;

import net.thenextlvl.worlds.api.event.WorldCloneEvent;
import net.thenextlvl.worlds.api.level.Level;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Interface representing a view for managing levels in a server environment.
 * It provides methods for reading, validating, and managing world directories and data.
 *
 * @since 2.0.0
 */
@NullMarked
@ApiStatus.NonExtendable
public interface LevelView {
    /**
     * Retrieves the path to the backup folder.
     *
     * @return the {@link Path} representing the backup folder
     * @since 3.0.0
     */
    @Contract(pure = true)
    Path getBackupFolder();

    /**
     * Retrieves the path to the backup folder for the specified world.
     *
     * @param world the world for which to retrieve the backup folder
     * @return the {@link Path} representing the backup folder for the specified world
     * @since 3.7.0
     */
    @Contract(pure = true)
    Path getBackupFolder(World world);

    /**
     * Retrieves the path to the world container directory.
     *
     * @return the {@link Path} representing the world container directory
     * @since 3.0.0
     */
    @Contract(pure = true)
    Path getWorldContainer();

    /**
     * Retrieves the permission required to enter the specified world.
     *
     * @param world the world for which to retrieve the entry permission
     * @return the permission string required to enter the world
     * @since 3.8.0
     */
    @Contract(pure = true)
    String getEntryPermission(World world);

    /**
     * Reads a level from the specified directory path.
     *
     * @param directory the directory containing the level data to be read
     * @return an {@code Optional} containing the {@code Level.Builder} if the directory represents a valid level,
     * or {@link Optional#empty()} if the directory is invalid
     * @since 3.0.0
     */
    @Contract(value = "_ -> new", pure = true)
    Optional<Level.Builder> read(Path directory);

    /**
     * Retrieves the plugin associated with the world generator for the given world, if it exists.
     *
     * @param world the world whose generator plugin is to be retrieved
     * @return an {@code Optional} containing the associated {@code JavaPlugin}, or {@link Optional#empty()}
     * if the world does not have a generator or if the generator is not associated with a plugin
     * @since 3.0.0
     */
    @Contract(pure = true)
    Optional<JavaPlugin> getGenerator(World world);

    /**
     * Returns an unmodifiable set of paths representing the directories of all levels
     * available in the server's {@link Server#getWorldContainer() world container}.
     * These directories are determined to be valid levels using specific validation criteria.
     *
     * @return an unmodifiable set of {@link Path} objects representing valid level directories,
     * or an empty set if no valid levels are found or if an error occurs while accessing the filesystem.
     * @since 3.0.0
     */
    @Unmodifiable
    @Contract(pure = true)
    Set<Path> listLevels();

    /**
     * Determines if a level located at the specified path can be loaded.
     * A level can be loaded if there is no other world loaded with the same path.
     *
     * @param level the path to the level directory to be checked
     * @return true if the level can be loaded, otherwise false
     * @since 3.0.0
     */
    @Contract(pure = true)
    boolean canLoad(Path level);

    /**
     * Determines whether the specified directory contains an End dimension folder.
     *
     * @param level the path to the directory to be checked
     * @return true if the level directory contains a {@code DIM1} folder, otherwise false
     * @since 3.0.0
     */
    @Contract(pure = true)
    boolean hasEndDimension(Path level);

    /**
     * Determines whether the specified directory contains a Nether dimension folder.
     *
     * @param level the path to the directory to be checked
     * @return true if the level directory contains a {@code DIM-1} folder, otherwise false
     * @since 3.0.0
     */
    @Contract(pure = true)
    boolean hasNetherDimension(Path level);

    /**
     * Determines whether the specified directory represents a valid world folder.
     *
     * @param path the path to the directory being checked
     * @return {@code true} if the directory contains a {@code level.dat}
     * or {@code level.dat_old}, otherwise {@code false}
     * @since 3.0.0
     */
    @Contract(pure = true)
    boolean isLevel(Path path);

    /**
     * Unloads the specified world from the server.
     *
     * @param world the world to be unloaded
     * @param save  whether changes to the world should be saved before unloading
     * @return true if the world was successfully unloaded, otherwise false
     * @since 3.0.0
     * @deprecated use {@link #unloadAsync(World, boolean)}
     */
    @Contract(mutates = "io,param1")
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
     * @since 3.2.0
     */
    @Contract(mutates = "io,param1")
    CompletableFuture<Boolean> unloadAsync(World world, boolean save);

    /**
     * Saves the specified world, with an option to flush pending changes immediately.
     *
     * @param world the world to be saved
     * @param flush whether to flush pending changes to disk immediately
     * @since 3.0.0
     * @deprecated use {@link #saveAsync(World, boolean)}
     */
    @Contract(mutates = "io,param1")
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
     * @since 3.2.0
     */
    @Contract(mutates = "io,param1")
    CompletableFuture<Void> saveAsync(World world, boolean flush);

    /**
     * Saves the {@code level.dat} of the specified world to disk.
     *
     * @param world the world whose level data should be saved
     * @param async whether the save operation should be performed asynchronously
     * @deprecated use {@link #saveLevelDataAsync(World)}
     */
    @Contract(mutates = "io,param1")
    @Deprecated(forRemoval = true, since = "3.2.0")
    default void saveLevelData(World world, boolean async) {
        var future = saveLevelDataAsync(world);
        if (!async) future.join();
    }

    /**
     * Saves the {@code level.dat} of the specified world to disk.
     *
     * @param world the world whose level data should be saved
     * @since 3.2.0
     */
    @Contract(mutates = "io,param1")
    CompletableFuture<Void> saveLevelDataAsync(World world);

    /**
     * Determines if the specified world is enabled and will be loaded on startup.
     *
     * @param world the world to check
     * @return true if the world is enabled, false otherwise
     * @since 3.2.0
     */
    @Contract(pure = true)
    boolean isEnabled(World world);

    /**
     * Sets whether the specified world is enabled and will be loaded on startup.
     *
     * @param world   the world to enable or disable
     * @param enabled true to enable the world, false to disable it
     * @since 3.2.0
     */
    @Contract(mutates = "param1")
    void setEnabled(World world, boolean enabled);

    /**
     * Creates a backup for the given world and returns the size of the backup in bytes.
     *
     * @param world the world to back up
     * @return the size of the created backup in bytes
     * @throws IOException if an I/O error occurs while creating the backup
     * @since 3.0.0
     * @deprecated use {@link #backupAsync(World)}
     */
    @Contract(mutates = "io,param1")
    @SuppressWarnings("RedundantThrows")
    @Deprecated(forRemoval = true, since = "3.3.1")
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
     * @since 3.2.0
     * @deprecated use {@link #createBackupAsync(World)}
     */
    @Contract(mutates = "io,param1")
    @Deprecated(forRemoval = true, since = "3.7.0")
    default CompletableFuture<Long> backupAsync(World world) {
        return createBackupAsync(world).thenApply(path -> {
            try {
                return Files.size(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to calculate backup size for " + path, e);
            }
        });
    }

    /**
     * Creates a backup for the given world with a default name.
     * <p>
     * Completes with an {@link IOException} if an I/O error occurs while creating the backup
     *
     * @param world the world to back up
     * @return A {@code CompletableFuture} completing with the path to the created backup
     * @since 3.7.0
     */
    @Contract(mutates = "io,param1")
    default CompletableFuture<Path> createBackupAsync(World world) {
        return createBackupAsync(world, null);
    }

    /**
     * Creates a backup for the given world with a specified name.
     * <p>
     * Completes with an {@link IOException} if an I/O error occurs while creating the backup
     * Completes with a {@link FileAlreadyExistsException} if a backup with the specified name already exists
     *
     * @param world the world to back up
     * @param name  the name for the backup file, or {@code null} to generate a default name
     * @return A {@code CompletableFuture} completing with the path to the created backup
     * @since 3.7.0
     */
    @Contract(mutates = "io,param1")
    CompletableFuture<Path> createBackupAsync(World world, @Nullable String name);

    /**
     * Restores a backup for the given world from the specified backup file.
     * <p>
     * Completes with an {@link IOException} if an I/O error occurs while restoring the backup
     *
     * @param world      the world to restore the backup for
     * @param backupFile the path to the backup file
     * @return A {@code CompletableFuture} completing with the restored world
     * @since 3.7.0
     */
    @Contract(mutates = "io,param1")
    CompletableFuture<RestoringResult> restoreBackupAsync(World world, Path backupFile, boolean schedule);

    /**
     * Cancels the backup restoration process for the specified world, if scheduled.
     *
     * @param world the world for which the scheduled backup restoration should be canceled
     * @return true if the scheduled backup restoration was successfully canceled, false if no restoration was scheduled
     * @since 3.7.0
     */
    @Contract(mutates = "this")
    boolean cancelScheduledBackupRestoration(World world);

    /**
     * Checks whether a backup restoration process is scheduled for the specified world.
     *
     * @param world the world to check for a scheduled backup restoration
     * @return true if a backup restoration process is scheduled for the world, otherwise false
     * @since 3.7.0
     */
    @Contract(pure = true)
    boolean isBackupRestorationScheduled(World world);

    /**
     * Lazily lists the available backups for the specified world.
     *
     * @param world the world for which to list the backups
     * @return A {@code Stream} of {@code Path} objects representing the backup files
     * @apiNote The resulting stream must be closed by the caller to release system resources
     * @since 3.7.0
     */
    @Contract(pure = true)
    Stream<Path> listBackups(World world);

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
     * @since 3.0.0
     * @deprecated use {@link #cloneAsync(World, Consumer, boolean)}
     */
    @Contract(mutates = "io,param1")
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
     * @since 3.2.0
     */
    @Contract(mutates = "io,param1")
    CompletableFuture<World> cloneAsync(World world, Consumer<Level.Builder> builder, boolean full);

    /**
     * Deletes the specified world from the server and disk.
     * The deletion can be executed immediately or scheduled for later, depending on the provided parameters.
     *
     * @param world    the world to be deleted
     * @param schedule if true, the deletion process will be scheduled for a later operation
     *                 (e.g., during server shutdown); if false, the deletion will be attempted immediately
     * @return a {@code DeletionResult} indicating the outcome of the deletion process.
     * @since 3.0.0
     * @deprecated use {@link #deleteAsync(World, boolean)}
     */
    @Contract(mutates = "io,param1")
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
     * @since 3.2.0
     */
    @Contract(mutates = "io,param1")
    CompletableFuture<DeletionResult> deleteAsync(World world, boolean schedule);

    /**
     * Cancels the deletion process for the specified world, if scheduled.
     *
     * @param world the world for which the scheduled deletion should be canceled
     * @return true if the scheduled deletion was successfully canceled, false if no deletion was scheduled
     * @since 3.0.0
     */
    @Contract(mutates = "this")
    boolean cancelScheduledDeletion(World world);

    /**
     * Checks whether a deletion process is scheduled for the specified world.
     *
     * @param world the world to check for a scheduled deletion
     * @return true if a deletion process is scheduled for the world, otherwise false
     * @since 3.0.0
     */
    @Contract(pure = true)
    boolean isDeletionScheduled(World world);

    /**
     * Regenerates the specified world, either immediately or scheduled, based on the provided parameters.
     *
     * @param world    the world to be regenerated
     * @param schedule if true, the regeneration will be scheduled for later execution;
     *                 if false, the regeneration will be attempted immediately
     * @return a {@code DeletionResult} indicating the outcome of the regeneration process.
     * @since 3.0.0
     * @deprecated use {@link #regenerateAsync(World, boolean)}
     */
    @Contract(mutates = "io,param1")
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
     * @since 3.2.0
     */
    @Contract(mutates = "io,param1")
    CompletableFuture<DeletionResult> regenerateAsync(World world, boolean schedule);

    /**
     * Cancels the regeneration process for the specified world, if scheduled.
     *
     * @param world the world for which the scheduled regeneration should be canceled
     * @return true if the scheduled regeneration was successfully canceled, false if no regeneration was scheduled
     * @since 3.0.0
     */
    @Contract(mutates = "this")
    boolean cancelScheduledRegeneration(World world);

    /**
     * Checks whether a regeneration process is scheduled for the specified world.
     *
     * @param world the world to check for a scheduled regeneration
     * @return true if a regeneration process is scheduled for the world, otherwise false
     * @since 3.0.0
     */
    @Contract(pure = true)
    boolean isRegenerationScheduled(World world);

    /**
     * Represents the outcome of a deletion process that results in the creation of a new world.
     *
     * @since 3.7.0
     */
    interface RestoringResult {
        /**
         * Retrieves the restored world, if available.
         *
         * @return the restored world, or {@code null} if the restoration did not complete with
         * {@link DeletionResult#SUCCESS}
         */
        @Nullable
        @Contract(pure = true)
        World world();

        /**
         * Retrieves the result of the restoration process.
         *
         * @return the result of the restoration process
         */
        @Contract(pure = true)
        DeletionResult result();
    }

    /**
     * Represents the possible outcomes of a world deletion process.
     *
     * @since 3.0.0
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
        @Contract(pure = true)
        public boolean isSuccess() {
            return this == SUCCESS || this == SCHEDULED;
        }
    }
}
