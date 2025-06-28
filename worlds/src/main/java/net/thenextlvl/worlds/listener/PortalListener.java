package net.thenextlvl.worlds.listener;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.model.PortalCooldown;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.END_PORTAL;

@NullMarked
public class PortalListener implements Listener {
    private final PortalCooldown cooldown = new PortalCooldown();
    private final WorldsPlugin plugin;

    public PortalListener(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPortalEnter(EntityPortalEnterEvent event) {
        if (!WorldsPlugin.RUNNING_FOLIA) return;
        if (!event.getPortalType().equals(PortalType.NETHER)) return;
        if (event.getEntity().getPortalCooldown() != 0) return;
        System.out.println(event.getEntity().getName() + " entered " + event.getPortalType() + " portal");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalReadyEvent event) {
        plugin.linkProvider().getTarget(event.getEntity().getWorld(), event.getPortalType())
                .ifPresentOrElse(event::setTargetWorld, () -> event.setTargetWorld(null));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (!event.getPortalType().equals(PortalType.ENDER)) return;

        event.setCancelled(true);

        if (!cooldown.start(plugin, event.getEntity())) return;

        var readyEvent = new EntityPortalReadyEvent(event.getEntity(), null, PortalType.ENDER);
        onEntityPortal(readyEvent);

        var targetWorld = readyEvent.getTargetWorld();
        if (targetWorld == null) return;

        if (targetWorld.getEnvironment().equals(World.Environment.THE_END)) {
            var spawn = new Location(targetWorld, 100.5, 49, 0.5, 90, 0);
            plugin.getServer().getRegionScheduler().run(plugin, spawn, scheduledTask -> {
                generateEndPlatform(targetWorld);
                event.getEntity().teleportAsync(spawn, END_PORTAL);
            });
        } else if (event.getEntity() instanceof CraftPlayer player) {
            Consumer<@Nullable Location> teleport = location -> player.getScheduler().run(plugin, scheduledTask -> {
                var level = ((CraftWorld) player.getWorld()).getHandle();
                if (WorldsPlugin.RUNNING_FOLIA || level.paperConfig().misc.disableEndCredits)
                    player.getHandle().seenCredits = true;
                else if (!player.getHandle().seenCredits) player.getHandle().showEndCredits();
                player.teleportAsync(Objects.requireNonNullElseGet(location, targetWorld::getSpawnLocation), END_PORTAL);
            }, null);
            var potentialLocation = player.getPotentialRespawnLocation();
            if (WorldsPlugin.RUNNING_FOLIA && potentialLocation != null) {
                plugin.getServer().getRegionScheduler().run(plugin, potentialLocation, task ->
                        teleport.accept(player.getRespawnLocation(true)));
            } else teleport.accept(player.getRespawnLocation(true));
        } else event.getEntity().getScheduler().run(plugin, task ->
                event.getEntity().teleportAsync(targetWorld.getSpawnLocation(), END_PORTAL), null);
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
