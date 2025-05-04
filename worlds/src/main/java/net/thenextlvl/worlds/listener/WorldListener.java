package net.thenextlvl.worlds.listener;

import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.event.WorldDeleteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener implements Listener {
    private final WorldsPlugin plugin;

    public WorldListener(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldDelete(WorldDeleteEvent event) {
        var provider = plugin.groupProvider();
        if (provider != null) provider.getGroup(event.getWorld()).ifPresent(group -> {
            if (group.removeWorld(event.getWorld())) return;
            plugin.getComponentLogger().error("Failed to removed deleted world {} from group {}",
                    event.getWorld().getName(), group.getName());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.linkProvider().loadTree(event.getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldUnloadEvent event) {
        plugin.linkProvider().unloadTree(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldSave(WorldSaveEvent event) {
        plugin.linkProvider().persistTree(event.getWorld());
    }
}
