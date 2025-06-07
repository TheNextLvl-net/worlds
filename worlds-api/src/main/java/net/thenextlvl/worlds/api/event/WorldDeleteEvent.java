package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Represents an event triggered when a {@link World} is deleted.
 * This event allows developers to listen to or cancel the deletion of a world.
 * It also provides an option to check if the deleted world is slated for regeneration.
 *
 * @see WorldActionScheduledEvent
 * @see WorldRegenerateEvent
 */
@NullMarked
public class WorldDeleteEvent extends WorldEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private boolean cancelled = false;

    @ApiStatus.Internal
    public WorldDeleteEvent(World world) {
        super(world, false);
    }

    /**
     * Gets whether this event will regenerate the world after deletion.
     *
     * @return whether the world is being regenerated
     * @see WorldRegenerateEvent
     */
    public boolean isRegenerating() {
        return false;
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
