package net.thenextlvl.worlds.view;

import org.bukkit.plugin.Plugin;

public interface GeneratorView {
    boolean hasGenerator(Plugin plugin);

    boolean hasChunkGenerator(Class<? extends Plugin> clazz);

    boolean hasBiomeProvider(Class<? extends Plugin> clazz);
}
