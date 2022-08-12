package net.nonswag.tnl.world.api.events;

import net.nonswag.tnl.world.api.world.TNLWorld;

import javax.annotation.Nonnull;

public class WorldDeleteEvent extends WorldEvent {

    public WorldDeleteEvent(@Nonnull TNLWorld world) {
        super(world);
    }
}
