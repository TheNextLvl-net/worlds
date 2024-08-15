package net.thenextlvl.worlds.api.event;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;

public class WorldDeleteEvent extends WorldEvent {
    private static final @Getter HandlerList handlerList = new HandlerList();

    public WorldDeleteEvent(World world) {
        super(world, false);
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
