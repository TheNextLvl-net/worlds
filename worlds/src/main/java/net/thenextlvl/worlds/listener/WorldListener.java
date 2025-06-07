package net.thenextlvl.worlds.listener;

import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.event.WorldDeleteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class WorldListener implements Listener {
    private final WorldsPlugin plugin;

    public WorldListener(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldDelete(WorldDeleteEvent event) {
        plugin.groupProvider().getGroup(event.getWorld()).ifPresent(group -> {
            if (group.removeWorld(event.getWorld())) return;
            plugin.getComponentLogger().error("Failed to remove deleted world {} from group {}",
                    event.getWorld().getName(), group.getName());
        });
    }
}
