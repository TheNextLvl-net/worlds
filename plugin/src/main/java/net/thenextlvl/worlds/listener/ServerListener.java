package net.thenextlvl.worlds.listener;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.DirectoryLock;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.exception.GeneratorException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

@RequiredArgsConstructor
public class ServerListener implements Listener {
    private final WorldsPlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        if (!event.getWorld().key().asString().equals("minecraft:overworld")) return;
        plugin.levelView().listLevels()
                .filter(plugin.levelView()::canLoad)
                .forEach(levelDirectory -> {
                    try {
                        var level = plugin.levelBuilder(levelDirectory).build();
                        if (!level.enabled()) return;
                        level.create().ifPresent(world -> plugin.getComponentLogger().debug(
                                "Loaded dimension {} ({}) from {}",
                                world.key().asString(), level.type().key().asString(),
                                world.getWorldFolder().getPath()
                        ));
                    } catch (GeneratorException e) {
                        var generator = e.getId() != null ? e.getPlugin() + e.getId() : e.getPlugin();
                        plugin.getComponentLogger().error("Skip loading dimension {}", levelDirectory.getName());
                        plugin.getComponentLogger().error("Cannot use generator {}: {}", generator, e.getMessage());
                    } catch (Exception e) {
                        if (e.getCause() instanceof DirectoryLock.LockException lock) {
                            plugin.getComponentLogger().error("Failed to start the minecraft server", lock);
                            plugin.getServer().shutdown();
                            return;
                        }
                        plugin.getComponentLogger().error("An unexpected error occurred while loading the level {}",
                                levelDirectory.getName(), e);
                        plugin.getComponentLogger().error("Please report the error above on GitHub: {}",
                                "https://github.com/TheNextLvl-net/worlds/issues/new/choose");
                    }
                });
    }
}
