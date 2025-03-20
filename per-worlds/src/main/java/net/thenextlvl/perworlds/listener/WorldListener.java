package net.thenextlvl.perworlds.listener;

import net.thenextlvl.perworlds.GroupProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldListener implements Listener {
    private final GroupProvider groupProvider;

    public WorldListener(GroupProvider groupProvider) {
        this.groupProvider = groupProvider;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var from = groupProvider.getGroup(event.getFrom());
        var to = groupProvider.getGroup(event.getPlayer().getWorld());
        if (from.equals(to)) return;
        from.ifPresent(group -> group.persist(event.getPlayer()));
        to.ifPresent(group -> group.load(event.getPlayer()));
    }
}
