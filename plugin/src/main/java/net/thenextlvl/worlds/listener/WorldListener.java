package net.thenextlvl.worlds.listener;

import net.thenextlvl.worlds.link.Link;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class WorldListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        var from = event.getFrom().getWorld();
        var worldLink = Link.links().filter(link -> switch (from.getEnvironment()) {
            case NORMAL -> from.getName().equals(link.overworld());
            case NETHER -> from.getName().equals(link.nether());
            case THE_END -> from.getName().equals(link.end());
            case CUSTOM -> from.getName().equals(link.custom());
        }).findFirst().orElse(null);
        if (worldLink == null) return;
        var target = switch (event.getCause()) {
            case END_PORTAL, END_GATEWAY -> worldLink.end();
            case NETHER_PORTAL -> worldLink.nether();
            default -> worldLink.custom();
        };
        if (target == null) return;
        // teleport to target world
    }
}
