package net.thenextlvl.worlds.versions;

import org.bukkit.World;
import org.bukkit.event.Listener;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public abstract class FoliaSupport {
    protected final PluginAccess plugin;

    public FoliaSupport(final PluginAccess plugin) {
        this.plugin = plugin;
    }

    public abstract CompletableFuture<@Nullable Void> saveAsync(World world, boolean flush);

    public abstract boolean canUnload(World world);

    public abstract void closeLevel(World world, boolean save);

    public abstract void removeLevel(World world);

    public abstract Listener createPortalListener();
}
