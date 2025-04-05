package net.thenextlvl.perworlds.listener;

import net.thenextlvl.perworlds.GroupProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class WorldListener implements Listener {
    private final GroupProvider groupProvider;

    public WorldListener(GroupProvider groupProvider) {
        this.groupProvider = groupProvider;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // todo: remove, just for testing
        Bukkit.getWorlds().forEach(world -> {
            if (groupProvider.hasGroup(world)) return;
            groupProvider.createGroup(world.getName(), settings -> {
            }, world);
        });

        groupProvider.getGroup(event.getPlayer().getWorld()).ifPresent(group ->
                group.loadPlayerData(event.getPlayer(), true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        groupProvider.getGroup(event.getPlayer().getWorld()).ifPresent(group ->
                group.persistPlayerData(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) return;
        var from = groupProvider.getGroup(event.getFrom().getWorld());
        var to = groupProvider.getGroup(event.getTo().getWorld());
        if (from.equals(to)) return;
        from.ifPresent(group -> group.persistPlayerData(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var from = groupProvider.getGroup(event.getFrom());
        var to = groupProvider.getGroup(event.getPlayer().getWorld());
        if (!from.equals(to)) to.ifPresent(group -> group.loadPlayerData(event.getPlayer(), false));
    }
}
