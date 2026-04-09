package net.thenextlvl.worlds.versions.v1_21_8;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.thenextlvl.worlds.WorldsAccess;
import net.thenextlvl.worlds.versions.PluginAccess;
import net.thenextlvl.worlds.versions.PortalHandler;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftEntity;

import java.util.Arrays;
import java.util.function.Consumer;

public final class FoliaPortalListener extends PortalHandler<PortalProcessor> {
    public FoliaPortalListener(final PluginAccess plugin) {
        super(plugin);
    }

    @Override
    protected void handlePortal(final org.bukkit.entity.Entity entity, final Location location) {
        final var block = (CraftBlock) location.getBlock();
        if (!(block.getNMS().getBlock() instanceof final NetherPortalBlock portal)) return;

        final var handle = ((CraftEntity) entity).getHandle();
        setAsInsidePortal(handle, portal, block.getPosition());

        handlePortal(block.getPosition(), handle);

        final var portalProcessor = portalProcessors.get(entity.getUniqueId());
        final var time = portalProcessor != null ? portalProcessor.getPortalTime() : 0;

        entity.getScheduler().runDelayed(plugin, scheduledTask -> {
            portalProcessors.computeIfPresent(entity.getUniqueId(), (uuid, processor) -> {
                if (processor.isSamePortal(portal) && processor.getPortalTime() > time) return processor;
                return null;
            });
        }, () -> {
            portalProcessors.remove(entity.getUniqueId());
            tickCounters.remove(entity.getUniqueId());
        }, 2);
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
        WorldsAccess.access().getTarget(entity.getBukkitEntity().getWorld(), PortalType.NETHER).ifPresent(world -> {
            try {
                final var level = ((CraftWorld) world).getHandle();
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
                plugin.getErrorTracker().trackError(e);
            }
        });
    }
}
