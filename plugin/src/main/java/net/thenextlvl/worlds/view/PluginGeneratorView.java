package net.thenextlvl.worlds.view;

import net.thenextlvl.worlds.api.view.GeneratorView;
import org.bukkit.plugin.Plugin;

public class PluginGeneratorView implements GeneratorView {
    @Override
    public boolean hasGenerator(Plugin plugin) {
        return hasChunkGenerator(plugin.getClass()) || hasBiomeProvider(plugin.getClass());
    }

    @Override
    public boolean hasChunkGenerator(Class<? extends Plugin> clazz) {
        try {
            return clazz.getMethod("getDefaultWorldGenerator", String.class, String.class).getDeclaringClass().equals(clazz);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public boolean hasBiomeProvider(Class<? extends Plugin> clazz) {
        try {
            return clazz.getMethod("getDefaultBiomeProvider", String.class, String.class).getDeclaringClass().equals(clazz);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
