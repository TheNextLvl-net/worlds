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
        assert plugin.configFile() != null;
        var location = event.getPlayer().hasPlayedBefore()
                ? plugin.configFile().getRoot().getJoinLocation()
                : plugin.configFile().getRoot().getFirstJoinLocation();
        if (location != null) event.setSpawnLocation(location);
    }
}
