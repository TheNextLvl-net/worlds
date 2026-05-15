package net.thenextlvl.worlds.event;

import net.kyori.adventure.key.Key;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.nio.file.Path;

/**
 * Represents an event triggered when a world folder is migrated from the pre-26.1
 * format to the new world folder format.
 * <p>
 * This event allows developers to perform manual file and directory operations
 * during migration.
 *
 * @since 4.2.0
 */
public final class WorldFolderMigrateEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    private final Key worldKey;
    private final Path newFolder;
    private final Path oldFolder;
    private final String newWorldName;
    private final String oldWorldName;

    @ApiStatus.Internal
    public WorldFolderMigrateEvent(final Key worldKey, final Path newFolder, final Path oldFolder, final String newWorldName, final String oldWorldName) {
        this.worldKey = worldKey;
        this.newFolder = newFolder;
        this.oldFolder = oldFolder;
        this.newWorldName = newWorldName;
        this.oldWorldName = oldWorldName;
    }

    /**
     * Retrieves the key of the world being migrated.
     *
     * @return the key of the world being migrated
     * @since 4.2.0
     */
    @Contract(pure = true)
    public Key getWorldKey() {
        return worldKey;
    }

    /**
     * Retrieves the new folder the world is being migrated to.
     *
     * @return the new world folder
     * @since 4.2.0
     */
    @Contract(pure = true)
    public Path getNewFolder() {
        return newFolder;
    }

    /**
     * Retrieves the old folder the world is being migrated from.
     *
     * @return the old world folder
     * @since 4.2.0
     */
    @Contract(pure = true)
    public Path getOldFolder() {
        return oldFolder;
    }

    /**
     * Retrieves the backwards-compatible world name after migration.
     * <p>
     * Worlds no longer have names, only keys. For backwards compatibility, world names
     * are now the raw key of the world with {@code _} instead of {@code :} and {@code /}.
     * Prefer {@link #getWorldKey()} for new code.
     *
     * @return the backwards-compatible world name after migration
     * @see #getWorldKey()
     * @since 4.2.0
     */
    @Contract(pure = true)
    public String getNewWorldName() {
        return newWorldName;
    }

    /**
     * Retrieves the legacy name of the world before migration.
     *
     * @return the name of the world before migration
     * @since 4.2.0
     */
    @Contract(pure = true)
    public String getOldWorldName() {
        return oldWorldName;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
