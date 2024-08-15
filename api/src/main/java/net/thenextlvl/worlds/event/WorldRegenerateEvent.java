package net.thenextlvl.worlds.event;

import org.bukkit.World;

public class WorldRegenerateEvent extends WorldDeleteEvent {
    public WorldRegenerateEvent(World world) {
        super(world);
    }
}
