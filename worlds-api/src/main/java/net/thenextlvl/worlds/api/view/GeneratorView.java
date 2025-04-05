package net.thenextlvl.worlds.api.view;

import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GeneratorView {
    boolean hasGenerator(Plugin plugin);

    boolean hasChunkGenerator(Class<? extends Plugin> clazz);

    boolean hasBiomeProvider(Class<? extends Plugin> clazz);
}
