package net.nonswag.tnl.world.api.events;

import lombok.Getter;
import lombok.Setter;
import net.nonswag.tnl.world.api.world.TNLWorld;
import org.bukkit.event.Cancellable;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@Setter
@ParametersAreNonnullByDefault
public class WorldDeleteEvent extends WorldEvent implements Cancellable {
    private boolean cancelled;

    public WorldDeleteEvent(TNLWorld world) {
        super(world);
    }
}
