package net.thenextlvl.worlds.image;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.Nullable;

public record Generator(String plugin, @Nullable String id) {

    @Nullable
    public static Generator of(World world) {
        var plugin = getGeneratorPlugin(world);
        return plugin != null ? new Generator(plugin.getName(), null) : null;
    }

    @Nullable
    @SuppressWarnings("UnstableApiUsage")
    public static Plugin getGeneratorPlugin(World world) {
        if (world.getGenerator() == null) return null;
        var loader = world.getGenerator().getClass().getClassLoader();
        if (!(loader instanceof PluginClassLoader pluginLoader)) return null;
        return pluginLoader.getPlugin();
    }

    public static boolean hasChunkGenerator(Class<? extends Plugin> clazz) {
        try {
            return clazz.getMethod("getDefaultWorldGenerator", String.class, String.class).getDeclaringClass().equals(clazz);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean hasBiomeProvider(Class<? extends Plugin> clazz) {
        try {
            return clazz.getMethod("getDefaultBiomeProvider", String.class, String.class).getDeclaringClass().equals(clazz);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return id() != null ? plugin() + ":" + id() : plugin();
    }
}
