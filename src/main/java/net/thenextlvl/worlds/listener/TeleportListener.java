package net.thenextlvl.worlds.listener;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class TeleportListener implements Listener {
    private final WorldsPlugin plugin;

    public TeleportListener(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        var world = event.getTo().getWorld();
        if (event.getPlayer().hasPermission(plugin.levelView().getEntryPermission(world))) return;
        plugin.bundle().sendMessage(event.getPlayer(), "world.entry.denied", 
                Placeholder.parsed("world", world.getName()));
        event.setCancelled(true);
    }
}
