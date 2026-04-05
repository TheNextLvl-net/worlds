package net.thenextlvl.worlds.view;

import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.versions.FoliaSupport;
import org.bukkit.World;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class FoliaLevelView extends PaperLevelView {
    private final FoliaSupport foliaSupport;

    public FoliaLevelView(final WorldsPlugin plugin, final FoliaSupport support) {
        super(plugin);
        this.foliaSupport = support;
    }

    @Override
    public CompletableFuture<@Nullable Void> saveAsync(final World world, final boolean flush) {
        final var saveFuture = foliaSupport.saveAsync(world, flush);
        final var levelDataFuture = saveLevelDataAsync(world);
        return CompletableFuture.allOf(saveFuture, levelDataFuture);
    }

    @Override
    public CompletableFuture<Boolean> unloadAsync(final World world, final boolean save) {
        if (!foliaSupport.canUnload(world))
            return CompletableFuture.completedFuture(false);

        return plugin.supplyGlobal(() -> {
            return CompletableFuture.completedFuture(new WorldUnloadEvent(world).callEvent());
        }).thenCompose(success -> {
            if (!success) return CompletableFuture.completedFuture(false);

            final var saving = save ? saveAsync(world, true) : CompletableFuture.<@Nullable Void>completedFuture(null);

            return saving.handle((result, throwable) -> {
                if (throwable != null) {
                    plugin.getComponentLogger().error("Error during world save", throwable);
                }

                return plugin.supplyGlobal(() -> {
                    try {
                        foliaSupport.closeLevel(world, save);
                    } catch (final Exception e) {
                        plugin.getComponentLogger().error("Failed to properly close world after saving", e);
                        WorldsPlugin.ERROR_TRACKER.trackError(e);
                    }
                    return CompletableFuture.<Void>completedFuture(null);
                });
            }).thenCompose(self -> self).thenApply(ignored -> {
                try {
                    final var server = plugin.getServer();
                    final var serverClass = server.getClass();
                    final var field = serverClass.getDeclaredField("worlds");
                    field.trySetAccessible();
                    @SuppressWarnings("unchecked") final var worlds = (Map<String, World>) field.get(server);
                    worlds.remove(world.getName().toLowerCase(Locale.ROOT));
                } catch (final NoSuchFieldException | IllegalAccessException e) {
                    plugin.getComponentLogger().error("Failed to remove world from memory", e);
                    WorldsPlugin.ERROR_TRACKER.trackError(e);
                    return false;
                }

                foliaSupport.removeLevel(world);

                return true;
            });
        });
    }
}
