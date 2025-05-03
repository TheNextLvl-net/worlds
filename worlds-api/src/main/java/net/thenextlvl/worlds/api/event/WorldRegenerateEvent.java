package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldRegenerateEvent extends WorldDeleteEvent {
    @ApiStatus.Internal
    public WorldRegenerateEvent(World world) {
        super(world);
    }
}
