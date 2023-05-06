package net.thenextlvl.worlds.volume;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.Nullable;

public record Generator(String plugin, @Nullable String id) {

    @Nullable
    @SuppressWarnings("UnstableApiUsage")
    public static Generator of(World world) {
        if (world.getGenerator() == null) return null;
        var loader = world.getGenerator().getClass().getClassLoader();
        if (!(loader instanceof PluginClassLoader pluginLoader)) return null;
        if (pluginLoader.getPlugin() == null) return null;
        return new Generator(pluginLoader.getPlugin().getName(), null);
    }

    public static boolean hasChunkGenerator(Class<? extends Plugin> clazz) {
        try {
            return clazz.getMethod("getDefaultWorldGenerator", String.class, String.class).getDeclaringClass().equals(clazz);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hasBiomeProvider(Class<? extends Plugin> clazz) {
        try {
            return clazz.getMethod("getDefaultBiomeProvider", String.class, String.class).getDeclaringClass().equals(clazz);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return id() != null ? plugin() + ":" + id() : plugin();
    }
}
