package net.nonswag.tnl.world.api.events;

import net.nonswag.tnl.listener.api.event.TNLEvent;
import org.bukkit.World;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class WorldEvent extends TNLEvent {

    @Nonnull
    private final World world;

    protected WorldEvent(@Nonnull World world) {
        this.world = world;
    }

    @Nonnull
    public World getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "WorldEvent{" +
                "world=" + world +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldEvent that = (WorldEvent) o;
        return world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world);
    }
}
