package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

/**
 * Represents an event triggered when a {@link World} backup is restored.
 * This event allows developers to perform custom logic before
 * the existing world is removed and the backup is restored.
 *
 * @see WorldActionScheduledEvent
 * @since 3.7.0
 */
@NullMarked
public final class WorldBackupRestoreEvent extends WorldEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private boolean cancelled = false;
    private final Path backupFile;

    @ApiStatus.Internal
    public WorldBackupRestoreEvent(final World world, final Path backupFile) {
        super(world, false);
        this.backupFile = backupFile;
    }

    @Contract(pure = true)
    public Path getBackupFile() {
        return backupFile;
    }

    @Override
    @Contract(pure = true)
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    @Contract(mutates = "this")
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
