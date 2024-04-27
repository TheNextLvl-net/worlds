package net.thenextlvl.worlds.listener;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.link.Link;
import net.thenextlvl.worlds.util.PortalCooldown;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;

import java.util.stream.IntStream;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.END_PORTAL;

@RequiredArgsConstructor
public class PortalListener implements Listener {
    private final PortalCooldown cooldown = new PortalCooldown();
    private final Worlds plugin;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalReadyEvent event) {
        plugin.linkRegistry().getLinks()
                .filter(link -> event.getPortalType().equals(link.portalType()))
                .filter(link -> event.getEntity().getWorld().equals(link.source()))
                .findFirst()
                .map(Link::destination)
                .ifPresent(event::setTargetWorld);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (!event.getLocation().getBlock().getType().equals(Material.END_PORTAL)) return;
        if (!cooldown.start(plugin, event.getEntity())) return;
        var readyEvent = new EntityPortalReadyEvent(event.getEntity(), null, PortalType.ENDER);
        if (!readyEvent.callEvent() || readyEvent.getTargetWorld() == null) return;
        if (readyEvent.getTargetWorld().getEnvironment().equals(World.Environment.THE_END)) {
            generateEndPlatform(readyEvent.getTargetWorld());
            var spawn = new Location(readyEvent.getTargetWorld(), 100.5, 50, 0.5, 90, 0);
            event.getEntity().teleportAsync(spawn, END_PORTAL);
        } else if (readyEvent.getTargetWorld().getEnvironment().equals(World.Environment.NETHER)) {
            var spawn = event.getLocation().clone();
            spawn.setWorld(readyEvent.getTargetWorld());
            spawn.setX(spawn.getX() * readyEvent.getTargetWorld().getCoordinateScale());
            spawn.setZ(spawn.getZ() * readyEvent.getTargetWorld().getCoordinateScale());
            event.getEntity().teleportAsync(spawn, END_PORTAL);
        } else if (event.getEntity() instanceof Player player) {
            var location = player.getRespawnLocation();
            if (location == null || !location.getWorld().equals(readyEvent.getTargetWorld()))
                player.teleportAsync(readyEvent.getTargetWorld().getSpawnLocation(), END_PORTAL);
            else player.teleportAsync(location, END_PORTAL);
        } else event.getEntity().teleportAsync(readyEvent.getTargetWorld().getSpawnLocation(), END_PORTAL);
    }

    private void generateEndPlatform(World world) {
        var platform = new Location(world, 100, 49, 0);
        IntStream.rangeClosed(platform.getBlockX() - 2, platform.getBlockX() + 2).forEach(x ->
                IntStream.rangeClosed(platform.getBlockZ() - 2, platform.getBlockZ() + 2).forEach(z -> {
                    var platformBlock = platform.getWorld().getBlockAt(x, platform.getBlockY() - 1, z);
                    if (!platformBlock.getType().equals(Material.OBSIDIAN))
                        platformBlock.setType(Material.OBSIDIAN);
                    IntStream.rangeClosed(1, 3).forEach(y -> {
                        var block = platformBlock.getRelative(BlockFace.UP, y);
                        if (!block.getType().equals(Material.AIR)) block.setType(Material.AIR);
                    });
                })
        );
    }
}
