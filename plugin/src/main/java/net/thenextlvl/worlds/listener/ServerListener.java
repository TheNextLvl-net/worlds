package net.thenextlvl.worlds.listener;

import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

@RequiredArgsConstructor
public class ServerListener implements Listener {
    private final WorldsPlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        if (!event.getWorld().key().asString().equals("minecraft:overworld")) return;
        plugin.levelView().listLevels()
                .filter(plugin.levelView()::canLoad)
                .forEach(level -> {
                    try {
                        var world = plugin.levelView().loadLevel(level);
                        if (world != null) plugin.getComponentLogger().debug("Loaded dimension {} at {}",
                                world.key().asString(), level.getPath());
                        else plugin.getComponentLogger().error("Failed to load the level {}", level.getPath());
                    } catch (Exception e) {
                        plugin.getComponentLogger().error("An unexpected error occurred while loading the level {}",
                                level.getPath(), e);
                        plugin.getComponentLogger().error("Please report the error above on GitHub: {}",
                                "https://github.com/TheNextLvl-net/worlds/issues/new/choose");
                    }
                });
    }
}
