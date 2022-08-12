package net.nonswag.tnl.world.api.events;

import lombok.Getter;
import net.nonswag.tnl.listener.api.event.TNLEvent;
import net.nonswag.tnl.world.api.world.TNLWorld;

import javax.annotation.Nonnull;
import java.util.Objects;

@Getter
public abstract class WorldEvent extends TNLEvent {

    @Nonnull
    private final TNLWorld world;

    protected WorldEvent(@Nonnull TNLWorld world) {
        this.world = world;
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
