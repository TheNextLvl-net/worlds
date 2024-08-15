package net.thenextlvl.worlds.api.model;

import com.google.common.base.Preconditions;
import net.thenextlvl.worlds.api.WorldsProvider;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public record Generator(Plugin plugin, @Nullable String id) {
    public String serialize() {
        return id != null ? plugin.getName() + ":" + id : plugin.getName();
    }

    public static Generator deserialize(WorldsProvider provider, String serialized) {
        var split = serialized.split(":", 2);
        var generator = provider.getServer().getPluginManager().getPlugin(split[0]);
        Preconditions.checkNotNull(generator, "Unknown plugin");
        Preconditions.checkState(generator.isEnabled(), "Plugin is not enabled");
        Preconditions.checkState(provider.generatorView().hasGenerator(generator), "Plugin has no generator");
        return new Generator(generator, split.length > 1 ? split[1] : null);
    }

    public @Nullable ChunkGenerator generator(String worldName) {
        return plugin().getDefaultWorldGenerator(worldName, id());
    }

    public @Nullable BiomeProvider biomeProvider(String worldName) {
        return plugin().getDefaultBiomeProvider(worldName, id());
    }
}
