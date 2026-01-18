package net.thenextlvl.worlds.listener;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.PortalType;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class FoliaPortalListener extends PortalListener {
    private static final int SEARCH_RADIUS_OVERWORLD = 128;
    private static final int SEARCH_RADIUS_NETHER = 16;

    private final Map<UUID, PortalProcessor> portalProcessors = new ConcurrentHashMap<>();

    public FoliaPortalListener(WorldsPlugin plugin) {
        super(plugin);
    }

    /**
     * @see NetherPortalBlock#entityInside(BlockState, Level, BlockPos, Entity, InsideBlockEffectApplier)
     */
    @SuppressWarnings("JavadocReference")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNetherPortalEnter(EntityPortalEnterEvent event) {
        if (!event.getPortalType().equals(PortalType.NETHER)) return;

        event.setCancelled(true);
        var location = event.getLocation();

        var block = (CraftBlock) location.getBlock();
        if (!(block.getNMS().getBlock() instanceof NetherPortalBlock portal)) return;

        var handle = ((CraftEntity) event.getEntity()).getHandle();
        setAsInsidePortal(handle, portal, block.getPosition());

        handlePortal(block.getPosition(), handle);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(EntityRemoveEvent event) {
        portalProcessors.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        portalProcessors.remove(event.getPlayer().getUniqueId());
    }

    /**
     * @see net.minecraft.world.entity.Entity#setAsInsidePortal(Portal, BlockPos)
     */
    private void setAsInsidePortal(net.minecraft.world.entity.Entity entity, Portal portal, BlockPos pos) {
        if (entity.isOnPortalCooldown()) {
            entity.setPortalCooldown();
        } else {
            var process = portalProcessors.get(entity.getUUID());
            if (process == null || !process.isSamePortal(portal)) {
                portalProcessors.put(entity.getUUID(), new PortalProcessor(portal, pos.immutable()));
            } else if (!process.isInsidePortalThisTick()) {
                process.updateEntryPosition(pos.immutable());
                process.setAsInsidePortalThisTick(true);
            }
        }
    }

    /**
     * @see net.minecraft.world.entity.Entity#handlePortal()
     */
    @SuppressWarnings("resource")
    private void handlePortal(BlockPos position, net.minecraft.world.entity.Entity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;
        var processor = portalProcessors.get(entity.getUUID());
        if (processor == null) return;

        processPortalCooldown(entity);
        if (processor.processPortalTeleportation(serverLevel, entity, entity.canUsePortal(false))) {
            var profilerFiller = Profiler.get();
            profilerFiller.push("portal");
            entity.setPortalCooldown();

            entity.getBukkitEntity().getScheduler().run(plugin, scheduledTask -> {
                try {
                    performPlayerTeleport(entity, processor, position);
                } finally {
                    profilerFiller.pop();
                }
            }, null);
        } else if (processor.hasExpired()) {
            portalProcessors.remove(entity.getUUID());
        }
    }

    /**
     * @see net.minecraft.world.entity.Entity#processPortalCooldown()
     */
    @SuppressWarnings("JavadocReference")
    private void processPortalCooldown(net.minecraft.world.entity.Entity entity) {
        if (entity.isOnPortalCooldown()) entity.portalCooldown--;
    }

    /**
     * @see Entity#netherPortalLogicAsync(BlockPos)
     */
    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> void performPlayerTeleport(net.minecraft.world.entity.Entity entity, PortalProcessor processor, BlockPos position) {
        var readyEvent = new EntityPortalReadyEvent(entity.getBukkitEntity(), null, PortalType.NETHER);
        onEntityPortal(readyEvent);

        portalProcessors.remove(entity.getUUID());

        var targetWorld = readyEvent.getTargetWorld();
        if (targetWorld != null) try {
            var level = ((CraftWorld) targetWorld).getHandle();
            var portalType = Arrays.stream(net.minecraft.world.entity.Entity.class.getDeclaredClasses())
                    .filter(c -> c.getSimpleName().equals("PortalType"))
                    .findAny().orElseThrow(() -> new IllegalStateException("PortalType class not found"));

            var portalToAsync = net.minecraft.world.entity.Entity.class.getDeclaredMethod(
                    "portalToAsync", ServerLevel.class, BlockPos.class, boolean.class, portalType, Consumer.class
            );
            var access = portalToAsync.canAccess(entity);
            if (!access) portalToAsync.setAccessible(true);
            var nether = Enum.valueOf((Class<T>) portalType, "NETHER");
            portalToAsync.invoke(entity, level, position, true, nether, null);
            portalToAsync.setAccessible(access);
        } catch (Exception e) {
            plugin.getComponentLogger().error("Failed to find portalToAsync method for Entity class", e);
            WorldsPlugin.ERROR_TRACKER.trackError(e);
        }
    }
}
