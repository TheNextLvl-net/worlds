package net.thenextlvl.perworlds.listener;

import net.thenextlvl.perworlds.GroupProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WorldListener implements Listener {
    private final GroupProvider provider;

    public WorldListener(GroupProvider provider) {
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        provider.getGroup(event.getPlayer().getWorld()).ifPresent(group ->
                group.loadPlayerData(event.getPlayer(), true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        provider.getGroup(event.getPlayer().getWorld()).ifPresent(group ->
                group.persistPlayerData(event.getPlayer()));
    }

    // todo: fix respawning not persisting data

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) return;
        var from = provider.getGroup(event.getFrom().getWorld());
        var to = provider.getGroup(event.getTo().getWorld());
        if (from.equals(to)) return;
        from.ifPresent(group -> group.persistPlayerData(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var from = provider.getGroup(event.getFrom());
        var to = provider.getGroup(event.getPlayer().getWorld());
        if (!from.equals(to)) to.ifPresent(group -> group.loadPlayerData(event.getPlayer(), false));
    }
}
