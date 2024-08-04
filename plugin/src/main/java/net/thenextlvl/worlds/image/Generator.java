package net.thenextlvl.worlds.image;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public record Generator(String plugin, @Nullable String id) {

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
