package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Represents an event triggered when a {@link World} is cloned.
 * This event allows developers to listen to the cloning process and modify
 * or cancel it if necessary. It provides information about whether the entire
 * world, including all data and entities, is being cloned, or if only the
 * configuration file is used for generation.
 */
@NullMarked
public class WorldCloneEvent extends WorldEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private boolean cancelled = false;
    private final boolean full;

    @ApiStatus.Internal
    public WorldCloneEvent(World world, boolean full) {
        super(world, false);
        this.full = full;
    }

    /**
     * Indicates whether the entire world, including entities, data, regions, etc. will be cloned
     * or only the {@code level.dat} file, so that the world is just generated with the same config.
     *
     * @return true if the entire world will be cloned, false, if only the {@code level.dat} file will be cloned
     */
    public boolean isFullClone() {
        return full;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
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
