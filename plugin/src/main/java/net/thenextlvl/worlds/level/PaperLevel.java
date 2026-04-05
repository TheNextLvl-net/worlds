package net.thenextlvl.worlds.level;

import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
final class PaperLevel extends LevelData {
    public PaperLevel(final WorldsPlugin plugin, final Builder builder) {
        super(plugin, builder);
    }

    @Override
    public CompletableFuture<World> createAsync() {
        return plugin.supplyGlobal(() -> plugin.handler().createAsync(this, plugin.levelView()));
    }
}
