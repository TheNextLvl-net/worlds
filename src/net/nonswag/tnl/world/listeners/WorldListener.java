package net.nonswag.tnl.world.listeners;

import net.nonswag.tnl.listener.api.logger.Logger;
import net.nonswag.tnl.world.Worlds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.io.File;
import java.util.List;

public class WorldListener implements Listener {

    @EventHandler
    public void onInitializeWorld(WorldInitEvent event) {
        if (!Worlds.getConfigUtil().isSet(event.getWorld().getWorldFolder().getAbsolutePath())) {
            List<String> worlds = Worlds.getWorlds();
            if (!worlds.contains(event.getWorld().getWorldFolder().getAbsolutePath())) {
                worlds.add(event.getWorld().getWorldFolder().getAbsolutePath());
                try {
                    Worlds.getConfigUtil().set("worlds", worlds);
                    Worlds.getConfigUtil().set(event.getWorld().getName() + ".type", event.getWorld().getWorldType().name());
                    Worlds.getConfigUtil().set(event.getWorld().getName() + ".environment", event.getWorld().getEnvironment().name());
                    // generator name/plugin
                } catch (Exception e) {
                    Logger.error.println(e);
                }
            }
        }
        List<String> worlds = Worlds.getWorlds();
        worlds.removeIf(s -> !new File(s).exists());
    }

    @EventHandler
    public void onLoadWorld(WorldLoadEvent event) {
        if (!Worlds.getConfigUtil().isSet(event.getWorld().getWorldFolder().getAbsolutePath())) {
            List<String> worlds = Worlds.getWorlds();
            if (!worlds.contains(event.getWorld().getWorldFolder().getAbsolutePath())) {
                worlds.add(event.getWorld().getWorldFolder().getAbsolutePath());
                Worlds.getConfigUtil().set("worlds", worlds);
            }
        }
        List<String> worlds = Worlds.getWorlds();
        worlds.removeIf(s -> !new File(s).exists());
    }

    @EventHandler
    public void onSaveWorld(WorldSaveEvent event) {
        if (!Worlds.getConfigUtil().isSet(event.getWorld().getWorldFolder().getAbsolutePath())) {
            List<String> worlds = Worlds.getWorlds();
            if (!worlds.contains(event.getWorld().getWorldFolder().getAbsolutePath())) {
                worlds.add(event.getWorld().getWorldFolder().getAbsolutePath());
                try {
                    Worlds.getConfigUtil().set("worlds", worlds);
                    Worlds.getConfigUtil().set(event.getWorld().getName() + ".type", event.getWorld().getWorldType().name());
                    Worlds.getConfigUtil().set(event.getWorld().getName() + ".environment", event.getWorld().getEnvironment().name());
                } catch (Exception e) {
                    Logger.error.println(e);
                }
            }
        }
        List<String> worlds = Worlds.getWorlds();
        worlds.removeIf(s -> !new File(s).exists());
    }
}
