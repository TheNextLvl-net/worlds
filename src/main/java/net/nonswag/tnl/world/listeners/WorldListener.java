package net.nonswag.tnl.world.listeners;

import net.nonswag.tnl.world.api.WorldUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;

import javax.annotation.Nonnull;

public class WorldListener implements Listener {

    @EventHandler
    public void onWorldEvent(@Nonnull WorldInitEvent event) {
        WorldUtil.getInstance().export(event.getWorld());
        WorldUtil.getInstance().saveWorlds();
    }

    @EventHandler
    public void onWorldEvent(@Nonnull WorldSaveEvent event) {
        WorldUtil.getInstance().export(event.getWorld());
        WorldUtil.getInstance().saveWorlds();
    }

    @EventHandler
    public void onWorldEvent(@Nonnull WorldLoadEvent event) {
        WorldUtil.getInstance().export(event.getWorld());
        WorldUtil.getInstance().saveWorlds();
    }
}
