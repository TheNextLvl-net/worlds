package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Represents an event triggered when a {@link World} is backed up.
 * This event allows developers to modify the world before it is written to disk.
 */
@NullMarked
public class WorldBackupEvent extends WorldEvent {
    private static final HandlerList handlerList = new HandlerList();

    @ApiStatus.Internal
    public WorldBackupEvent(World world) {
        super(world, false);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
