package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldDeleteEvent extends WorldEvent {
    private static final HandlerList handlerList = new HandlerList();

    public WorldDeleteEvent(World world) {
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
