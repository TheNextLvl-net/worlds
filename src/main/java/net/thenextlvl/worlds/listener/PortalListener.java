package net.thenextlvl.worlds.listener;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.model.PortalCooldown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.jspecify.annotations.NullMarked;

import java.util.stream.IntStream;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.END_PORTAL;

@NullMarked
@RequiredArgsConstructor
public class PortalListener implements Listener {
    private final PortalCooldown cooldown = new PortalCooldown();
    private final WorldsPlugin plugin;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalReadyEvent event) {
        if (event.getPortalType().equals(PortalType.CUSTOM)) return;
        plugin.linkController().getTarget(event.getEntity().getWorld(), event.getPortalType())
                .map(Bukkit::getWorld).ifPresentOrElse(event::setTargetWorld, () ->
                        event.setTargetWorld(null));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (!event.getPortalType().equals(PortalType.ENDER)) return;

        event.setCancelled(true);

        if (!cooldown.start(plugin, event.getEntity())) return;

        var readyEvent = new EntityPortalReadyEvent(event.getEntity(), null, PortalType.ENDER);
        onEntityPortal(readyEvent);

        if (readyEvent.getTargetWorld() == null) return;

        if (readyEvent.getTargetWorld().getEnvironment().equals(World.Environment.THE_END)) {
            generateEndPlatform(readyEvent.getTargetWorld());
            var spawn = new Location(readyEvent.getTargetWorld(), 100.5, 49, 0.5, 90, 0);
            event.getEntity().teleportAsync(spawn, END_PORTAL);
        } else if (event.getEntity() instanceof CraftPlayer player) {
            if (!player.getHandle().seenCredits) player.getHandle().showEndCredits();
            if (player.getRespawnLocation() != null) player.teleportAsync(player.getRespawnLocation(), END_PORTAL);
            else player.teleportAsync(readyEvent.getTargetWorld().getSpawnLocation(), END_PORTAL);
        } else event.getEntity().teleport(readyEvent.getTargetWorld().getSpawnLocation(), END_PORTAL);
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
