package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Represents an event triggered when a {@link World} is deleted and subsequently regenerated.
 * This event signifies that a world is scheduled for regeneration following its deletion,
 * providing an opportunity for developers to listen to or modify the regeneration process.
 */
@NullMarked
public class WorldRegenerateEvent extends WorldDeleteEvent {
    private static final HandlerList handlerList = new HandlerList();

    @ApiStatus.Internal
    public WorldRegenerateEvent(World world) {
        super(world);
    }

    @Override
    public boolean isRegenerating() {
        return true;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
