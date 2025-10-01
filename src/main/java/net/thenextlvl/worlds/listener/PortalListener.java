package net.thenextlvl.worlds.listener;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.portal.PortalShape;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.model.PortalCooldown;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.END_PORTAL;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.NETHER_PORTAL;

@NullMarked
public class PortalListener implements Listener {
    private final PortalCooldown cooldown = new PortalCooldown();
    private final WorldsPlugin plugin;

    private static final int SEARCH_RADIUS_OVERWORLD = 128;
    private static final int SEARCH_RADIUS_NETHER = 16;
    private static final double COORDINATE_SCALE = 8.0;

    private final Map<UUID, PlayerPortalStatus> playerPortalStatus = new ConcurrentHashMap<>();

    public PortalListener(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    private static class PlayerPortalStatus {
        int ticksInPortal = 0;
        Location portalLocation;
        boolean isTeleporting = false;
        boolean hasLeftPortal = true;

        PlayerPortalStatus(Location portalLocation) {
            this.portalLocation = portalLocation;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPortalEnter(EntityPortalEnterEvent event) {
        if (!WorldsPlugin.RUNNING_FOLIA) return;

        if (!event.getPortalType().equals(PortalType.NETHER)) return;

        event.setCancelled(true);

        if (event.getEntity() instanceof Player player) {
            handlePlayerPortalEnter(player, event.getLocation());
            return;
        }

        if (!cooldown.start(plugin, event.getEntity())) return;

        var readyEvent = new EntityPortalReadyEvent(event.getEntity(), null, PortalType.NETHER);
        onEntityPortal(readyEvent);

        var targetWorld = readyEvent.getTargetWorld();
        if (targetWorld == null) return;

        var entityLocation = event.getLocation();
        var targetLocation = calculateNetherPortalLocation(entityLocation, targetWorld);

        plugin.getServer().getRegionScheduler().run(plugin, targetLocation, scheduledTask -> {
            var portalLocation = findOrCreatePortal(targetLocation, targetWorld, event.getEntity());

            event.getEntity().teleportAsync(portalLocation, NETHER_PORTAL);
        });
    }

    private void handlePlayerPortalEnter(Player player, Location portalLocation) {
        var playerId = player.getUniqueId();
        var status = playerPortalStatus.get(playerId);

        if (status != null && status.isTeleporting) {
            return;
        }

        if (status != null && !status.hasLeftPortal) {
            return;
        }

        if (status == null || !isSamePortal(status.portalLocation, portalLocation)) {
            status = new PlayerPortalStatus(portalLocation);
            status.hasLeftPortal = false;
            playerPortalStatus.put(playerId, status);

            startPortalTimer(player, status);
        }
    }

    private boolean isSamePortal(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) return false;
        return loc1.distance(loc2) < 5.0;
    }

    private void startPortalTimer(Player player, PlayerPortalStatus status) {
        player.getScheduler().runDelayed(plugin, task -> {
            var currentBlock = player.getLocation().getBlock();
            if (currentBlock.getType() != Material.NETHER_PORTAL) {
                playerPortalStatus.remove(player.getUniqueId());
                return;
            }

            if (status.isTeleporting) {
                return;
            }

            status.ticksInPortal++;

            int requiredTicks = getRequiredPortalTicks(player);

            if (status.ticksInPortal >= requiredTicks) {
                status.isTeleporting = true;

                performPlayerTeleport(player);
            } else {
                startPortalTimer(player, status);
            }
        }, null, 1L);
    }

    private int getRequiredPortalTicks(Player player) {
        var world = player.getWorld();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return world.getGameRuleValue(GameRule.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY);
        } else {
            return world.getGameRuleValue(GameRule.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY);
        }
    }

    private void performPlayerTeleport(Player player) {
        var readyEvent = new EntityPortalReadyEvent(player, null, PortalType.NETHER);
        onEntityPortal(readyEvent);

        var targetWorld = readyEvent.getTargetWorld();
        if (targetWorld == null) {
            playerPortalStatus.remove(player.getUniqueId());
            return;
        }

        var entityLocation = player.getLocation();
        var targetLocation = calculateNetherPortalLocation(entityLocation, targetWorld);

        plugin.getServer().getRegionScheduler().run(plugin, targetLocation, scheduledTask -> {
            var portalLocation = findOrCreatePortal(targetLocation, targetWorld, player);

            player.teleportAsync(portalLocation, NETHER_PORTAL).thenAccept(success -> {
                if (success) {
                    var status = playerPortalStatus.get(player.getUniqueId());
                    if (status != null) {
                        status.hasLeftPortal = false;
                        status.isTeleporting = false;
                        status.ticksInPortal = 0;
                    }

                    if (player instanceof CraftPlayer craftPlayer) {
                        craftPlayer.getHandle().connection.send(
                                new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false)
                        );
                    }
                } else {
                    playerPortalStatus.remove(player.getUniqueId());
                }
            });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!WorldsPlugin.RUNNING_FOLIA) return;

        var player = event.getPlayer();
        var status = playerPortalStatus.get(player.getUniqueId());

        if (status != null) {
            var currentBlock = event.getTo().getBlock();
            if (currentBlock.getType() != Material.NETHER_PORTAL) {
                status.hasLeftPortal = true;
                status.ticksInPortal = 0;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!WorldsPlugin.RUNNING_FOLIA) return;

        playerPortalStatus.remove(event.getPlayer().getUniqueId());
    }

    private Location calculateNetherPortalLocation(Location from, World targetWorld) {
        double scale = 1.0;

        if (from.getWorld().getEnvironment() == World.Environment.NORMAL &&
                targetWorld.getEnvironment() == World.Environment.NETHER) {
            scale = 1.0 / COORDINATE_SCALE;
        }
        else if (from.getWorld().getEnvironment() == World.Environment.NETHER &&
                targetWorld.getEnvironment() == World.Environment.NORMAL) {
            scale = COORDINATE_SCALE;
        }

        double x = from.getX() * scale;
        double z = from.getZ() * scale;
        double y = Math.max(targetWorld.getMinHeight(), Math.min(from.getY(), targetWorld.getMaxHeight() - 1));

        return new Location(targetWorld, x, y, z, from.getYaw(), from.getPitch());
    }

    private Location findOrCreatePortal(Location target, World world, Entity entity) {
        var existingPortal = findExistingPortal(target, world);
        return existingPortal.orElseGet(() -> createNetherPortal(target, world, entity));
    }

    private Optional<Location> findExistingPortal(Location target, World world) {
        int searchRadius = world.getEnvironment() == World.Environment.NETHER
                ? SEARCH_RADIUS_NETHER
                : SEARCH_RADIUS_OVERWORLD;

        int centerX = target.getBlockX();
        int centerZ = target.getBlockZ();

        double closestDistanceSq = Double.MAX_VALUE;
        Location closestPortal = null;

        for (int x = centerX - searchRadius; x <= centerX + searchRadius; x++) {
            for (int z = centerZ - searchRadius; z <= centerZ + searchRadius; z++) {
                for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                    var block = world.getBlockAt(x, y, z);

                    if (block.getType() == Material.NETHER_PORTAL) {
                        double distanceSq = Math.pow(x - target.getX(), 2) +
                                Math.pow(y - target.getY(), 2) +
                                Math.pow(z - target.getZ(), 2);

                        if (distanceSq < closestDistanceSq) {
                            closestDistanceSq = distanceSq;
                            closestPortal = findPortalCenter(block);
                        } else if (distanceSq == closestDistanceSq && closestPortal != null) {
                            var currentCenter = findPortalCenter(block);
                            if (currentCenter.getY() < closestPortal.getY()) {
                                closestPortal = currentCenter;
                            }
                        }
                    }
                }
            }
        }

        return Optional.ofNullable(closestPortal);
    }

    private Location findPortalCenter(Block portalBlock) {
        var bottom = portalBlock;
        while (bottom.getRelative(BlockFace.DOWN).getType() == Material.NETHER_PORTAL) {
            bottom = bottom.getRelative(BlockFace.DOWN);
        }

        bottom = bottom.getRelative(BlockFace.UP);

        return bottom.getLocation().add(0.5, 0, 0.5);
    }

    private Location createNetherPortal(Location target, World world, Entity entity) {
        ServerLevel level = ((CraftWorld) world).getHandle();

        int targetY = target.getBlockY();
        if (world.getEnvironment() == World.Environment.NETHER) {
            targetY = Math.max(10, Math.min(targetY, 120));
        } else {
            targetY = Math.max(world.getMinHeight() + 5, Math.min(targetY, world.getMaxHeight() - 10));
        }

        BlockPos targetPos = new BlockPos(target.getBlockX(), targetY, target.getBlockZ());

        var nmsEntity = ((CraftEntity) entity).getHandle();

        var portalForcer = level.getPortalForcer();

        int searchRadius = world.getEnvironment() == World.Environment.NETHER
                ? SEARCH_RADIUS_NETHER
                : SEARCH_RADIUS_OVERWORLD;

        var portalPosition = portalForcer.findClosestPortalPosition(targetPos, level.getWorldBorder(), searchRadius);

        if (portalPosition.isPresent()) {
            var pos = portalPosition.get();
            return new Location(world,
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5
            );
        }

        var createdPortalZ = portalForcer.createPortal(targetPos, Direction.Axis.Z);

        if (createdPortalZ.isPresent()) {
            var rect = createdPortalZ.get();
            return new Location(world,
                    rect.minCorner.getX() + (rect.axis1Size / 2.0),
                    rect.minCorner.getY(),
                    rect.minCorner.getZ() + (rect.axis2Size / 2.0)
            );
        }

        var createdPortalX = portalForcer.createPortal(targetPos, Direction.Axis.X);

        if (createdPortalX.isPresent()) {
            var rect = createdPortalX.get();
            return new Location(world,
                    rect.minCorner.getX() + (rect.axis1Size / 2.0),
                    rect.minCorner.getY(),
                    rect.minCorner.getZ() + (rect.axis2Size / 2.0)
            );
        }

        Optional<PortalShape> shapeZ = PortalShape.findEmptyPortalShape(
                level, targetPos, Direction.Axis.Z
        );

        if (shapeZ.isPresent() && shapeZ.get().isValid()) {
            shapeZ.get().createPortalBlocks(level, nmsEntity);
            return new Location(world,
                    targetPos.getX() + 0.5,
                    targetPos.getY(),
                    targetPos.getZ() + 0.5
            );
        }

        Optional<PortalShape> shapeX = PortalShape.findEmptyPortalShape(
                level, targetPos, Direction.Axis.X
        );

        if (shapeX.isPresent() && shapeX.get().isValid()) {
            shapeX.get().createPortalBlocks(level, nmsEntity);
            return new Location(world,
                    targetPos.getX() + 0.5,
                    targetPos.getY(),
                    targetPos.getZ() + 0.5
            );
        }

        plugin.getLogger().warning("Failed to create nether portal at " +
                targetPos.toShortString() + " in world " + world.getName());
        return target;
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
                generateEndPlatform(targetWorld, event.getEntity());
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

    private void generateEndPlatform(World world, Entity entity) {
        var handle = ((CraftWorld) world).getHandle();
        var entityHandle = WorldsPlugin.RUNNING_FOLIA ? null : ((CraftEntity) entity).getHandle();
        EndPlatformFeature.createEndPlatform(handle, new BlockPos(100, 49, 0), true, entityHandle);
    }
}
