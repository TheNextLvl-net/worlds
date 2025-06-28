package net.thenextlvl.worlds.listener;

import net.kyori.adventure.util.TriState;
import net.minecraft.util.DirectoryLock;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.event.WorldDeleteEvent;
import net.thenextlvl.worlds.api.exception.GeneratorException;
import net.thenextlvl.worlds.api.level.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener implements Listener {
    private final WorldsPlugin plugin;

    public WorldListener(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldDelete(WorldDeleteEvent event) {
        var provider = plugin.groupProvider();
        if (provider != null) provider.getGroup(event.getWorld()).ifPresent(group -> {
            if (group.removeWorld(event.getWorld())) return;
            plugin.getComponentLogger().error("Failed to remove deleted world {} from group {}",
                    event.getWorld().getName(), group.getName());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onOverworldLoad(WorldLoadEvent event) {
        if (!plugin.levelView().isOverworld(event.getWorld())) return;
        plugin.levelView().listLevels().stream().filter(plugin.levelView()::canLoad).forEach(path -> {
            var level = plugin.levelView().read(path).map(Level.Builder::build).orElse(null);
            if (level == null || !level.isEnabled().equals(TriState.TRUE)) return;
            if (plugin.getServer().getWorld(level.key()) != null) return;
            if (plugin.getServer().getWorld(level.getName()) != null) return;
            level.createAsync().thenAccept(world -> plugin.getComponentLogger().debug(
                    "Loaded dimension {} ({}) from {}",
                    world.key().asString(), level.getGeneratorType().key().asString(),
                    world.getWorldFolder().getPath()
            )).exceptionally(throwable -> {
                if (throwable instanceof GeneratorException e) {
                    var generator = e.getId() != null ? e.getPlugin() + e.getId() : e.getPlugin();
                    plugin.getComponentLogger().error("Skip loading dimension '{}'", path.getFileName());
                    plugin.getComponentLogger().error("Cannot use generator {}: {}", generator, e.getMessage());
                } else if (throwable.getCause() instanceof DirectoryLock.LockException lock) {
                    plugin.getComponentLogger().error("Failed to start the minecraft server", lock);
                    plugin.getServer().shutdown();
                } else {
                    plugin.getComponentLogger().error("An unexpected error occurred while loading the level {}",
                            path.getFileName(), throwable);
                    plugin.getComponentLogger().error("Please report the error above on GitHub: {}",
                            "https://github.com/TheNextLvl-net/worlds/issues/new/choose");
                }
                return null;
            });
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.linkProvider().loadTree(event.getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldUnloadEvent event) {
        plugin.linkProvider().unloadTree(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldSave(WorldSaveEvent event) {
        plugin.linkProvider().persistTree(event.getWorld());
    }
}
