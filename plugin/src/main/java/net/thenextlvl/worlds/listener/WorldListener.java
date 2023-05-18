package net.thenextlvl.worlds.listener;

import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.link.Link;
import net.thenextlvl.worlds.link.PortalType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

@RequiredArgsConstructor
public class WorldListener implements Listener {
    private final Worlds plugin;

    @EventHandler(ignoreCancelled = true)
    public void onPortal(EntityPortalEvent event) {
        System.out.println(event.getEntity().getName());
        var from = event.getFrom().getWorld();
        var target = plugin.linkFile().links().stream()
                .filter(link -> switch (link.portalType()) {
                    case NETHER_PORTAL -> event.getPortalType().equals(org.bukkit.PortalType.NETHER);
                    case END_PORTAL, END_GATEWAY -> event.getPortalType().equals(org.bukkit.PortalType.ENDER);
                    case CUSTOM -> event.getPortalType().equals(org.bukkit.PortalType.CUSTOM);
                })
                .filter(link -> from.getName().equals(link.first()))
                .findFirst()
                .map(Link::second)
                .map(Bukkit::getWorld)
                .orElse(null);
        if (target == null || event.getTo() == null) return;
        event.getTo().setWorld(target);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        var from = event.getFrom().getWorld();
        var target = plugin.linkFile().links().stream()
                .filter(link -> switch (event.getCause()) {
                    case NETHER_PORTAL -> link.portalType().equals(PortalType.NETHER_PORTAL);
                    case END_PORTAL -> link.portalType().equals(PortalType.END_PORTAL);
                    case END_GATEWAY -> link.portalType().equals(PortalType.END_GATEWAY);
                    case PLUGIN -> link.portalType().equals(PortalType.CUSTOM);
                    default -> false;
                })
                .filter(link -> from.getName().equals(link.first()))
                .findFirst()
                .map(Link::second)
                .map(Bukkit::getWorld)
                .orElse(null);
        if (target == null) return;
        event.getTo().setWorld(target);
    }
}
