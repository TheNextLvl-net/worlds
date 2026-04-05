package net.thenextlvl.worlds.versions.v1_21_11;

import net.thenextlvl.worlds.versions.FoliaSupport;
import net.thenextlvl.worlds.versions.PluginAccess;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.Listener;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class SimpleFoliaSupport extends FoliaSupport {
    public SimpleFoliaSupport(final PluginAccess plugin) {
        super(plugin);
    }

    @Override
    public CompletableFuture<@Nullable Void> saveAsync(final World world, final boolean flush) {
        final var futures = new ArrayList<CompletableFuture<@Nullable Void>>();
        final var level = ((CraftWorld) world).getHandle();
        level.regioniser.computeForAllRegionsUnsynchronised(region -> {
            final var future = new CompletableFuture<@Nullable Void>();
            futures.add(future);
            final var location = region.getCenterChunk();
            if (location == null) {
                future.complete(null);
                return;
            }
            plugin.getServer().getRegionScheduler().run(plugin, world, location.x, location.z, task -> {
                try {
                    level.getChunkSource().save(flush);
                    future.complete(null);
                } catch (final Exception e) {
                    future.completeExceptionally(e);
                }
            });
        });
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public boolean canUnload(final World world) {
        final var handle = ((CraftWorld) world).getHandle();
        final var server = ((CraftServer) plugin.getServer());

        if (server.getServer().getLevel(handle.dimension()) == null) return false;
        if (handle.dimension() == net.minecraft.world.level.Level.OVERWORLD) return false;
        return handle.players().isEmpty();
    }

    @Override
    public void closeLevel(final World world, final boolean save) {
        final var handle = ((CraftWorld) world).getHandle();
        try {
            handle.getChunkSource().close(false);
            io.papermc.paper.FeatureHooks.closeEntityManager(handle, save);
            handle.levelStorageAccess.close();
        } catch (final Exception e) {
            throw new RuntimeException("Failed to close world", e);
        }
    }

    @Override
    public void removeLevel(final World world) {
        final var handle = ((CraftWorld) world).getHandle();
        final var server = ((CraftServer) plugin.getServer());
        server.getServer().removeLevel(handle);
        handle.regioniser.computeForAllRegionsUnsynchronised(regionThread -> {
            if (regionThread.getData().world != handle) return;
            regionThread.getData().getRegionSchedulingHandle().markNonSchedulable();
        });
    }

    @Override
    public Listener createPortalListener() {
        return new FoliaPortalListener(plugin);
    }
}
