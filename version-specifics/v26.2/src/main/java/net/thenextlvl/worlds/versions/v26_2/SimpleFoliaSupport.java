package net.thenextlvl.worlds.versions.v26_2;

import ca.spottedleaf.moonrise.patches.chunk_system.io.MoonriseRegionFileIO;
import io.papermc.paper.FeatureHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
            plugin.getServer().getRegionScheduler().run(plugin, world, location.x(), location.z(), task -> {
                try {
                    level.getChunkSource().save(false);
                    future.complete(null);
                } catch (final Exception e) {
                    future.completeExceptionally(e);
                }
            });
        });
        final var saved = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        return flush ? saved.thenCompose(ignored -> flushAsync(level)) : saved;
    }

    private CompletableFuture<@Nullable Void> flushAsync(final ServerLevel level) {
        final var future = new CompletableFuture<@Nullable Void>();
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                MoonriseRegionFileIO.flush(level);
                MoonriseRegionFileIO.flushRegionStorages(level);
                future.complete(null);
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public boolean canUnload(final World world) {
        final var handle = ((CraftWorld) world).getHandle();
        final var server = ((CraftServer) plugin.getServer());

        if (server.getServer().getLevel(handle.dimension()) == null) return false;
        if (handle.dimension() == Level.OVERWORLD) return false;
        return handle.players().isEmpty();
    }

    /**
     * @see CraftServer#unloadWorld(World, boolean)
     */
    @Override
    public void closeLevel(final World world, final boolean save) {
        try {
            final var handle = ((CraftWorld) world).getHandle();
            markNonSchedulable(handle);
            handle.getChunkSource().close(false);
            FeatureHooks.closeEntityManager(handle, save);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to close world", e);
        }
    }

    @Override
    public void removeLevel(final World world) {
        final var handle = ((CraftWorld) world).getHandle();
        final var server = ((CraftServer) plugin.getServer());
        server.getServer().removeLevel(handle);
        markNonSchedulable(handle);
    }

    private void markNonSchedulable(final ServerLevel handle) {
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
