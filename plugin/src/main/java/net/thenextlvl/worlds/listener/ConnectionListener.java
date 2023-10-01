package net.thenextlvl.worlds.listener;

import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

@RequiredArgsConstructor
public class ConnectionListener implements Listener {
    private final Worlds plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(PlayerSpawnLocationEvent event) {
        if (event.getPlayer().hasPlayedBefore()) {
            var world = plugin.configFile().getRoot().getJoinWorld();
            if (world != null) event.setSpawnLocation(world.getSpawnLocation());
        } else {
            var world = plugin.configFile().getRoot().getFirstJoinWorld();
            if (world != null) event.setSpawnLocation(world.getSpawnLocation());
        }
    }
}
