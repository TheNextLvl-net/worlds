package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

/**
 * Represents an event triggered when a {@link World} is deleted and subsequently regenerated.
 * This event signifies that a world is scheduled for regeneration following its deletion,
 * providing an opportunity for developers to listen to or modify the regeneration process.
 *
 * @see WorldActionScheduledEvent
 * @since 2.0.0
 */
@NullMarked
public final class WorldRegenerateEvent extends WorldEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private boolean cancelled = false;

    @ApiStatus.Internal
    public WorldRegenerateEvent(final World world) {
        super(world, false);
    }

    @Override
    @Contract(pure = true)
    public boolean isCancelled() {
        return this.cancelled;
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
