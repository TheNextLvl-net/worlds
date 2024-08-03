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
        plugin.levelView().listOverworldLevels()
                .filter(plugin.levelView()::canLoad)
                .forEach(plugin.levelView()::loadOverworldLevel);
        plugin.levelView().listNetherLevels()
                .filter(plugin.levelView()::canLoad)
                .forEach(plugin.levelView()::loadNetherLevel);
        plugin.levelView().listEndLevels()
                .filter(plugin.levelView()::canLoad)
                .forEach(plugin.levelView()::loadEndLevel);
    }
}
