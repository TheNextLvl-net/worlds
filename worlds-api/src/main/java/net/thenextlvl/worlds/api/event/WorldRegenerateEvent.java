package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

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
