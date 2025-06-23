package net.thenextlvl.worlds.view;

import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
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
            plugin.getServer().getRegionScheduler().run(plugin, level.getWorld(), location.x, location.z, task -> {
                try {
                    var chunkCache = level.getChunkSource();
                    chunkCache.save(flush);
                    future.complete(null);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        });
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }
}
