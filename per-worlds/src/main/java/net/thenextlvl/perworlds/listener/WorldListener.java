package net.thenextlvl.perworlds.listener;

import net.thenextlvl.perworlds.GroupProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WorldListener implements Listener {
    private final GroupProvider groupProvider;

    public WorldListener(GroupProvider groupProvider) {
        this.groupProvider = groupProvider;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        groupProvider.getGroup(event.getPlayer().getWorld()).ifPresent(group ->
                group.loadPlayerData(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        groupProvider.getGroup(event.getPlayer().getWorld()).ifPresent(group ->
                group.persistPlayerData(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var from = groupProvider.getGroup(event.getFrom());
        var to = groupProvider.getGroup(event.getPlayer().getWorld());
        if (from.equals(to)) return;
        from.ifPresent(group -> group.persistPlayerData(event.getPlayer()));
        to.ifPresent(group -> group.loadPlayerData(event.getPlayer()));
    }
}
