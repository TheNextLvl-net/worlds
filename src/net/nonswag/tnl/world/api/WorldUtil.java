package net.nonswag.tnl.world.api;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldUtil {

    @Nullable
    public static Plugin getGenerator(@Nonnull World world) {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            System.out.println(plugin.getDefaultWorldGenerator(world.getName(), null).getClass().getName());
            System.out.println(world.getGenerator().getClass().getName());
            if (plugin.getDefaultWorldGenerator(world.getName(), null).getClass().equals(world.getGenerator().getClass())) {
                return plugin;
            }
        }
        return null;
    }
}
