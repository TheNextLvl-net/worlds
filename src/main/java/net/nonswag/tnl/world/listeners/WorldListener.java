package net.nonswag.tnl.world.listeners;

import net.nonswag.tnl.world.api.WorldUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import javax.annotation.Nonnull;

public class WorldListener implements Listener {

    @EventHandler
    public void onWorldEvent(@Nonnull WorldInitEvent event) {
        WorldUtil.getInstance().save(event.getWorld());
    }

    @EventHandler
    public void onWorldEvent(@Nonnull WorldLoadEvent event) {
        WorldUtil.getInstance().save(event.getWorld());
    }
}
