package net.thenextlvl.perworlds.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.thenextlvl.perworlds.group.PaperGroupProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RespawnListener implements Listener {
    private final PaperGroupProvider provider;

    public RespawnListener(PaperGroupProvider provider) {
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.isBedSpawn() || event.isAnchorSpawn()) return;
        var group = provider.getGroup(event.getPlayer().getWorld())
                .orElse(provider.getUnownedWorldGroup());
        var location = group.getGroupData().spawnLocation();
        if (location != null) event.setRespawnLocation(location);
        else group.getSpawnLocation().ifPresent(event::setRespawnLocation);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        provider.getGroup(event.getPlayer().getWorld())
                .orElse(provider.getUnownedWorldGroup())
                .persistPlayerData(event.getPlayer());
    }
}
