package net.nonswag.tnl.world.api.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.nonswag.tnl.listener.api.event.TNLEvent;
import net.nonswag.tnl.world.api.world.TNLWorld;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public abstract class WorldEvent extends TNLEvent {
    private final TNLWorld world;

    protected WorldEvent(TNLWorld world) {
        this.world = world;
    }
}
