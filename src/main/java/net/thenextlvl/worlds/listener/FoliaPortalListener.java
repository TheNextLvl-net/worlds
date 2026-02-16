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
    private final Map<UUID, TickCounter> tickCounters = new ConcurrentHashMap<>();

    public FoliaPortalListener(final WorldsPlugin plugin) {
        super(plugin);
    }

    /**
     * Count the number of ticks since the last event was processed.
     * <p>
     * This is required since the {@link EntityPortalEnterEvent} is fired for each portal block the entity is touching.
     */
    private class TickCounter {
        private int lastTick;
        private int current;

        public TickCounter() {
            final var currentTick = plugin.getServer().getCurrentTick();
            this.lastTick = currentTick - 1;
            this.current = currentTick;
        }

        public TickCounter update() {
            final var currentTick = plugin.getServer().getCurrentTick();
            this.lastTick = this.current;
            this.current = currentTick;
            return this;
        }

        public boolean hasTickChanged() {
            return this.lastTick != this.current;
        }
    }

    /**
     * @see NetherPortalBlock#entityInside(BlockState, Level, BlockPos, Entity, InsideBlockEffectApplier)
     */
    @SuppressWarnings("JavadocReference")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNetherPortalEnter(final EntityPortalEnterEvent event) {
        if (!event.getPortalType().equals(PortalType.NETHER)) return;

        event.setCancelled(true);

        if (!tickCounters.compute(event.getEntity().getUniqueId(), (uuid, counter) -> {
            return counter != null ? counter.update() : new TickCounter();
        }).hasTickChanged()) return;

        final var location = event.getLocation();

        final var block = (CraftBlock) location.getBlock();
        if (!(block.getNMS().getBlock() instanceof final NetherPortalBlock portal)) return;

        final var handle = ((CraftEntity) event.getEntity()).getHandle();
        setAsInsidePortal(handle, portal, block.getPosition());

        handlePortal(block.getPosition(), handle);

        final var portalProcessor = portalProcessors.get(event.getEntity().getUniqueId());
        final var time = portalProcessor != null ? portalProcessor.getPortalTime() : 0;

        event.getEntity().getScheduler().runDelayed(plugin, scheduledTask -> {
            portalProcessors.computeIfPresent(event.getEntity().getUniqueId(), (uuid, processor) -> {
                if (processor.isSamePortal(portal) && processor.getPortalTime() > time) return processor;
                return null;
            });
        }, () -> {
            portalProcessors.remove(event.getEntity().getUniqueId());
            tickCounters.remove(event.getEntity().getUniqueId());
        }, 2);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(final EntityRemoveEvent event) {
        portalProcessors.remove(event.getEntity().getUniqueId());
        tickCounters.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        portalProcessors.remove(event.getPlayer().getUniqueId());
        tickCounters.remove(event.getPlayer().getUniqueId());
    }

    /**
     * @see Entity#setAsInsidePortal(Portal, BlockPos)
     */
    private void setAsInsidePortal(final Entity entity, final Portal portal, final BlockPos pos) {
        if (entity.isOnPortalCooldown()) {
            entity.setPortalCooldown();
        } else {
            final var process = portalProcessors.get(entity.getUUID());
            if (process == null || !process.isSamePortal(portal)) {
                portalProcessors.put(entity.getUUID(), new PortalProcessor(portal, pos.immutable()));
            } else if (!process.isInsidePortalThisTick()) {
                process.updateEntryPosition(pos.immutable());
                process.setAsInsidePortalThisTick(true);
            }
        }
    }

    /**
     * @see Entity#handlePortal()
     */
    @SuppressWarnings("resource")
    private void handlePortal(final BlockPos position, final Entity entity) {
        if (!(entity.level() instanceof final ServerLevel serverLevel)) return;
        final var processor = portalProcessors.get(entity.getUUID());
        if (processor == null) return;

        processPortalCooldown(entity);
        if (processor.processPortalTeleportation(serverLevel, entity, entity.canUsePortal(false))) {
            final var profilerFiller = Profiler.get();
            profilerFiller.push("portal");
            entity.setPortalCooldown();

            entity.getBukkitEntity().getScheduler().run(plugin, scheduledTask -> {
                try {
                    performPlayerTeleport(entity, processor, position);
                } finally {
                    portalProcessors.remove(entity.getUUID());
                    tickCounters.remove(entity.getUUID());
                    profilerFiller.pop();
                }
            }, null);
        } else if (processor.hasExpired()) {
            portalProcessors.remove(entity.getUUID());
            tickCounters.remove(entity.getUUID());
        }
    }

    /**
     * @see Entity#processPortalCooldown()
     */
    @SuppressWarnings("JavadocReference")
    private void processPortalCooldown(final Entity entity) {
        if (entity.isOnPortalCooldown()) entity.portalCooldown--;
    }

    /**
     * @see Entity#netherPortalLogicAsync(BlockPos)
     */
    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> void performPlayerTeleport(final Entity entity, final PortalProcessor processor, final BlockPos position) {
        final var readyEvent = new EntityPortalReadyEvent(entity.getBukkitEntity(), null, PortalType.NETHER);
        onEntityPortal(readyEvent);

        final var targetWorld = readyEvent.getTargetWorld();
        if (targetWorld != null) try {
            final var level = ((CraftWorld) targetWorld).getHandle();
            final var portalType = Arrays.stream(Entity.class.getDeclaredClasses())
                    .filter(c -> c.getSimpleName().equals("PortalType"))
                    .findAny().orElseThrow(() -> new IllegalStateException("PortalType class not found"));

            final var portalToAsync = Entity.class.getDeclaredMethod(
                    "portalToAsync", ServerLevel.class, BlockPos.class, boolean.class, portalType, Consumer.class
            );
            final var access = portalToAsync.canAccess(entity);
            if (!access) portalToAsync.setAccessible(true);
            final var nether = Enum.valueOf((Class<T>) portalType, "NETHER");
            portalToAsync.invoke(entity, level, position, true, nether, null);
            portalToAsync.setAccessible(access);
        } catch (final Exception e) {
            plugin.getComponentLogger().error("Failed to find portalToAsync method for Entity class", e);
            WorldsPlugin.ERROR_TRACKER.trackError(e);
        }
    }
}
