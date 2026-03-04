package net.thenextlvl.worlds.view;

import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public final class FoliaLevelView extends PaperLevelView {
    public FoliaLevelView(final WorldsPlugin plugin) {
        super(plugin);
    }

    @Override
    public CompletableFuture<Void> saveAsync(final World world, final boolean flush) {
        throw new UnsupportedOperationException("Folia is not supported in this version");
    }

    /**
     * @see CraftServer#unloadWorld(World, boolean)
     */
    @Override
    public CompletableFuture<Boolean> unloadAsync(final World world, final boolean save) {
        throw new UnsupportedOperationException("Folia is not supported in this version");
    }
}
