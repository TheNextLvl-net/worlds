package net.thenextlvl.worlds.api.generator;

import net.thenextlvl.worlds.api.WorldsProvider;
import net.thenextlvl.worlds.api.exception.GeneratorException;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * This record represents a generator that interacts with the Minecraft world generation system.
 * It encapsulates a plugin responsible for providing the generator and an optional identifier
 * for specifying a unique generator configuration.
 * <p>
 * The `Generator` record offers methods to serialize its state, retrieve chunk generators,
 * and obtain biome providers for specific world names.
 */
@NullMarked
public record Generator(Plugin plugin, @Nullable String id) {
    public String asString() {
        return id != null ? plugin.getName() + ":" + id : plugin.getName();
    }

    public @Nullable ChunkGenerator generator(String worldName) {
        return plugin().getDefaultWorldGenerator(worldName, id());
    }

    public @Nullable BiomeProvider biomeProvider(String worldName) {
        return plugin().getDefaultBiomeProvider(worldName, id());
    }

    public static Generator of(WorldsProvider provider, String string) throws GeneratorException {
        var split = string.split(":", 2);

        var plugin = split[0];
        var id = split.length > 1 ? split[1] : null;

        var generator = provider.getServer().getPluginManager().getPlugin(plugin);

        if (generator == null)
            throw new GeneratorException(plugin, id, "Unknown plugin");
        if (!generator.isEnabled())
            throw new GeneratorException(plugin, id, "Plugin is not enabled, is it 'load: STARTUP'?");
        if (!provider.generatorView().hasGenerator(generator))
            throw new GeneratorException(plugin, id, "Plugin has no generator");

        return new Generator(generator, id);
    }
}
