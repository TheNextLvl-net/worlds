package net.thenextlvl.worlds.listener;

import net.minecraft.util.DirectoryLock;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.event.WorldDeleteEvent;
import net.thenextlvl.worlds.api.exception.GeneratorException;
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
            plugin.getComponentLogger().error("Failed to removed deleted world {} from group {}",
                    event.getWorld().getName(), group.getName());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onOverworldLoad(WorldLoadEvent event) {
        if (!event.getWorld().key().asString().equals("minecraft:overworld")) return;
        plugin.levelView().listLevels().stream().filter(plugin.levelView()::canLoad).forEach(path -> {
            try {
                var level = plugin.levelBuilder(path).build();
                if (!level.isEnabled()) return;
                level.create().ifPresent(world -> plugin.getComponentLogger().debug(
                        "Loaded dimension {} ({}) from {}",
                        world.key().asString(), level.getGeneratorType().key().asString(),
                        world.getWorldFolder().getPath()
                ));
            } catch (GeneratorException e) {
                var generator = e.getId() != null ? e.getPlugin() + e.getId() : e.getPlugin();
                plugin.getComponentLogger().error("Skip loading dimension {}", path.getFileName());
                plugin.getComponentLogger().error("Cannot use generator {}: {}", generator, e.getMessage());
            } catch (Exception e) {
                if (e.getCause() instanceof DirectoryLock.LockException lock) {
                    plugin.getComponentLogger().error("Failed to start the minecraft server", lock);
                    plugin.getServer().shutdown();
                    return;
                }
                plugin.getComponentLogger().error("An unexpected error occurred while loading the level {}",
                        path.getFileName(), e);
                plugin.getComponentLogger().error("Please report the error above on GitHub: {}",
                        "https://github.com/TheNextLvl-net/worlds/issues/new/choose");
            }
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
