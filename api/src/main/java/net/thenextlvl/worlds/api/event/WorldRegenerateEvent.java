package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldRegenerateEvent extends WorldDeleteEvent {
    public WorldRegenerateEvent(World world) {
        super(world);
    }
}
