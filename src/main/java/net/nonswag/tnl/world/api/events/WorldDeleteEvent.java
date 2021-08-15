package net.nonswag.tnl.world.api.events;

import org.bukkit.World;

import javax.annotation.Nonnull;

public class WorldDeleteEvent extends WorldEvent {

    public WorldDeleteEvent(@Nonnull World world) {
        super(world);
    }
}
