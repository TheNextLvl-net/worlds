package net.thenextlvl.worlds.listener;

import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

@RequiredArgsConstructor
public class ServerListener implements Listener {
    private final WorldsPlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(ServerLoadEvent event) {
        if (!event.getType().equals(ServerLoadEvent.LoadType.STARTUP)) return;
        plugin.levelView().listLevels()
                .filter(plugin.levelView()::canLoad)
                .forEach(level -> {
                    try {
                        var world = plugin.levelView().loadLevel(level);
                    } catch (Exception e) {
                        plugin.getComponentLogger().error("Could not load level {}", level.getPath(), e);
                    }
                });
    }
}
