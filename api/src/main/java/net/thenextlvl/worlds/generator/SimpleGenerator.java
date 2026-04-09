package net.thenextlvl.worlds.generator;

import net.thenextlvl.worlds.WorldsAccess;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

final class SimpleGenerator implements Generator {
    private final @Nullable String id;
    private final Plugin plugin;

    public SimpleGenerator(final Plugin plugin, @Nullable final String id) {
        this.plugin = plugin;
        this.id = id;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    @Override
    public Optional<ChunkGenerator> generator(final String worldName) {
        return plugin.isEnabled() ? Optional.ofNullable(plugin.getDefaultWorldGenerator(worldName, id)) : Optional.empty();
    }

    @Override
    public Optional<BiomeProvider> biomeProvider(final String worldName) {
        return plugin.isEnabled() ? Optional.ofNullable(plugin.getDefaultBiomeProvider(worldName, id)) : Optional.empty();
    }

    @Override
    public String asString() {
        return id != null ? plugin.getName() + ":" + id : plugin.getName();
    }

    public static Generator of(final String string) throws GeneratorException {
        final var split = string.split(":", 2);

        final var plugin = split[0];
        final var id = split.length > 1 ? split[1] : null;

        final var generator = WorldsAccess.access().getServer().getPluginManager().getPlugin(plugin);

        if (generator == null)
            throw new GeneratorException(plugin, id, "Plugin not found");
        if (!generator.isEnabled())
            throw new GeneratorException(plugin, id, "Plugin is not enabled, is it 'load: STARTUP'?");
        if (!GeneratorView.view().hasGenerator(generator))
            throw new GeneratorException(plugin, id, "Plugin has no generator");

        return new SimpleGenerator(generator, id);
    }
}
