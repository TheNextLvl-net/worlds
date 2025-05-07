package net.thenextlvl.worlds.api.generator;

import net.thenextlvl.worlds.api.WorldsProvider;
import net.thenextlvl.worlds.api.exception.GeneratorException;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record Generator(Plugin plugin, @Nullable String id) {
    public String serialize() {
        return id != null ? plugin.getName() + ":" + id : plugin.getName();
    }

    public @Nullable ChunkGenerator generator(String worldName) {
        return plugin().getDefaultWorldGenerator(worldName, id());
    }

    public @Nullable BiomeProvider biomeProvider(String worldName) {
        return plugin().getDefaultBiomeProvider(worldName, id());
    }

    public static Generator deserialize(WorldsProvider provider, String serialized) throws GeneratorException {
        var split = serialized.split(":", 2);

        var plugin = split[0];
        var id = split.length > 1 ? split[1] : null;

        var generator = provider.getServer().getPluginManager().getPlugin(plugin);

        if (generator == null)
            throw new GeneratorException(plugin, id, "Unknown plugin");
        if (!provider.isEnabled())
            throw new GeneratorException(plugin, id, "Plugin is not enabled");
        if (!provider.generatorView().hasGenerator(generator))
            throw new GeneratorException(plugin, id, "Plugin has no generator");

        return new Generator(generator, id);
    }
}
