package net.thenextlvl.worlds.api.event;

import org.bukkit.World;

public class WorldRegenerateEvent extends WorldDeleteEvent {
    public WorldRegenerateEvent(World world) {
        super(world);
    }
}
