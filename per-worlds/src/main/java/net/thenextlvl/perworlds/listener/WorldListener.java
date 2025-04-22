package net.thenextlvl.perworlds.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.thenextlvl.perworlds.group.PaperGroupProvider;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldListener implements Listener {
    private final PaperGroupProvider provider;

    public WorldListener(PaperGroupProvider provider) {
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        provider.getGroup(event.getPlayer().getWorld())
                .orElse(provider.getUnownedWorldGroup())
                .loadPlayerData(event.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        provider.getGroup(event.getPlayer().getWorld())
                .orElse(provider.getUnownedWorldGroup())
                .persistPlayerData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.isBedSpawn() || event.isAnchorSpawn()) return;
        var group = provider.getGroup(event.getPlayer().getWorld())
                .orElse(provider.getUnownedWorldGroup());
        var location = group.getGroupData().spawnLocation();
        if (location != null) event.setRespawnLocation(location);
        else group.getWorlds().stream().filter(world -> world.getEnvironment() == Environment.NORMAL)
                .findAny().map(World::getSpawnLocation).ifPresent(event::setRespawnLocation);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        provider.getGroup(event.getPlayer().getWorld())
                .orElse(provider.getUnownedWorldGroup())
                .persistPlayerData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) return;
        var from = provider.getGroup(event.getFrom().getWorld()).orElse(provider.getUnownedWorldGroup());
        var to = provider.getGroup(event.getTo().getWorld()).orElse(provider.getUnownedWorldGroup());
        if (!from.equals(to)) from.persistPlayerData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var from = provider.getGroup(event.getFrom()).orElse(provider.getUnownedWorldGroup());
        var to = provider.getGroup(event.getPlayer().getWorld()).orElse(provider.getUnownedWorldGroup());
        if (!from.equals(to)) to.loadPlayerData(event.getPlayer(), false);
    }
}
