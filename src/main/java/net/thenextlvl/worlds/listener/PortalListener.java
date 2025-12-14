package net.thenextlvl.worlds.listener;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.model.PortalCooldown;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.END_PORTAL;

@NullMarked
public class PortalListener implements Listener {
    private final PortalCooldown cooldown = new PortalCooldown();
    protected final WorldsPlugin plugin;

    public PortalListener(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalReadyEvent event) {
        plugin.linkProvider().getTarget(event.getEntity().getWorld(), event.getPortalType())
                .ifPresentOrElse(event::setTargetWorld, () -> event.setTargetWorld(null));
    }

    /**
     * @see net.minecraft.world.level.block.EndPortalBlock#getPortalDestination(ServerLevel, net.minecraft.world.entity.Entity, BlockPos)
     * @see net.minecraft.world.entity.Entity#handlePortal()
     */
    @SuppressWarnings("JavadocReference")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (!event.getPortalType().equals(PortalType.ENDER)) return;

        event.setCancelled(true);

        if (!cooldown.start(plugin, event.getEntity())) return;

        var readyEvent = new EntityPortalReadyEvent(event.getEntity(), null, PortalType.ENDER);
        onEntityPortal(readyEvent);

        var targetWorld = readyEvent.getTargetWorld();
        if (targetWorld == null) return;

        if (plugin.levelView().isOverworld(event.getEntity().getWorld()) && plugin.levelView().isEnd(targetWorld)) {
            event.setCancelled(false); // Don't handle overworld to end teleportation to allow gravity block duping
            return;
        }

        if (targetWorld.getEnvironment().equals(World.Environment.THE_END)) {
            var spawn = new Location(targetWorld, 100.5, 49, 0.5, 90, 0);
            plugin.getServer().getRegionScheduler().run(plugin, spawn, scheduledTask -> {
                generateEndPlatform(targetWorld, event.getEntity());
                event.getEntity().teleportAsync(spawn, END_PORTAL);
            });
        } else if (event.getEntity() instanceof CraftPlayer player) {
            Consumer<@Nullable Location> teleport = location -> player.getScheduler().run(plugin, scheduledTask -> {
                var level = ((CraftWorld) player.getWorld()).getHandle();

                if (WorldsPlugin.RUNNING_FOLIA || level.paperConfig().misc.disableEndCredits) {
                    player.getHandle().seenCredits = true;
                } else if (!player.getHandle().seenCredits) {
                    player.getHandle().showEndCredits();
                }

                player.teleportAsync(Objects.requireNonNullElseGet(location, targetWorld::getSpawnLocation), END_PORTAL);
            }, null);

            var potentialLocation = player.getPotentialRespawnLocation();
            if (WorldsPlugin.RUNNING_FOLIA && potentialLocation != null) {
                plugin.getServer().getRegionScheduler().run(plugin, potentialLocation, task -> {
                    teleport.accept(player.getRespawnLocation(true));
                });
            } else teleport.accept(player.getRespawnLocation(true));

        } else event.getEntity().getScheduler().run(plugin, task -> {
            event.getEntity().teleportAsync(targetWorld.getSpawnLocation(), END_PORTAL);
        }, null);
    }

    private void generateEndPlatform(World world, Entity entity) {
        var handle = ((CraftWorld) world).getHandle();
        var entityHandle = WorldsPlugin.RUNNING_FOLIA ? null : ((CraftEntity) entity).getHandle();
        EndPlatformFeature.createEndPlatform(handle, new BlockPos(100, 49, 0), true, entityHandle);
    }
}
