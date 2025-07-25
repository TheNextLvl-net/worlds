package net.thenextlvl.worlds.view;

import io.papermc.paper.FeatureHooks;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class FoliaLevelView extends PaperLevelView {
    public FoliaLevelView(WorldsPlugin plugin) {
        super(plugin);
    }

    @Override
    public CompletableFuture<Void> saveAsync(World world, boolean flush) {
        var futures = new ArrayList<CompletableFuture<Void>>();
        var level = ((CraftWorld) world).getHandle();
        level.regioniser.computeForAllRegionsUnsynchronised(region -> {
            var future = new CompletableFuture<@Nullable Void>();
            futures.add(future);

            var location = region.getCenterChunk();
            plugin.getServer().getRegionScheduler().run(plugin, world, location.x, location.z, task -> {
                try {
                    level.getChunkSource().save(flush);
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
        });
        futures.add(saveLevelDataAsync(world));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /**
     * @see CraftServer#unloadWorld(World, boolean)
     */
    @Override
    public CompletableFuture<Boolean> unloadAsync(World world, boolean save) {
        var handle = ((CraftWorld) world).getHandle();
        var server = ((CraftServer) plugin.getServer());

        if (server.getServer().getLevel(handle.dimension()) == null)
            return CompletableFuture.completedFuture(false);

        if (handle.dimension() == net.minecraft.world.level.Level.OVERWORLD)
            return CompletableFuture.completedFuture(false);

        if (!handle.players().isEmpty())
            return CompletableFuture.completedFuture(false);


        var future = new CompletableFuture<Boolean>();

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            future.complete(new WorldUnloadEvent(handle.getWorld()).callEvent());
        });

        return future.thenCompose(success -> {
            if (!success) return CompletableFuture.completedFuture(false);

            var saving = save ? saveAsync(world, true) : CompletableFuture.completedFuture(null);

            return saving.handle((result, throwable) -> {
                if (throwable != null) {
                    plugin.getComponentLogger().error("Error during world save", throwable);
                }

                try {
                    handle.getChunkSource().close(false);
                    FeatureHooks.closeEntityManager(handle, save);
                    handle.levelStorageAccess.close();
                } catch (Exception ex) {
                    plugin.getComponentLogger().error("Failed to properly close world after saving", ex);
                }

                try {
                    var field = server.getClass().getDeclaredField("worlds");
                    field.trySetAccessible();
                    @SuppressWarnings("unchecked") var worlds = (Map<String, World>) field.get(server);
                    worlds.remove(world.getName().toLowerCase(Locale.ROOT));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    plugin.getComponentLogger().error("Failed to remove world from memory", throwable);
                    return false;
                }

                server.getServer().removeLevel(handle);

                handle.regioniser.computeForAllRegionsUnsynchronised(regionThread -> {
                    if (regionThread.getData().world != handle) return;
                    regionThread.getData().getRegionSchedulingHandle().markNonSchedulable();
                });

                return true;
            });
        });
    }
}
