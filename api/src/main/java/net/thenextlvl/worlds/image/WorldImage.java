package net.thenextlvl.worlds.image;

import core.api.file.format.GsonFile;
import org.bukkit.*;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public record WorldImage(
        String name,
        @Nullable String settings,
        @Nullable Generator generator,
        World.Environment environment,
        WorldType type,
        boolean generateStructures,
        boolean hardcore,
        boolean loadOnStart,
        long seed
) {

    @Nullable
    public World build() {
        var creator = new WorldCreator(name, new NamespacedKey("worlds", name.toLowerCase().replace(" ", "_")))
                .generator(resolveChunkGenerator())
                .biomeProvider(resolveBiomeProvider())
                .generateStructures(generateStructures())
                .generatorSettings(settings() != null ? settings() : "")
                .environment(environment())
                .hardcore(hardcore())
                .type(type())
                .seed(seed());
        return creator.createWorld();
    }

    @Nullable
    private ChunkGenerator resolveChunkGenerator() {
        if (generator() == null) return null;
        var plugin = Bukkit.getPluginManager().getPlugin(generator().plugin());
        if (plugin == null || !plugin.isEnabled())
            throw new IllegalArgumentException();
        return plugin.getDefaultWorldGenerator(name(), generator().id());
    }

    @Nullable
    private BiomeProvider resolveBiomeProvider() {
        if (generator() == null) return null;
        var plugin = Bukkit.getPluginManager().getPlugin(generator().plugin());
        if (plugin == null || !plugin.isEnabled())
            throw new IllegalArgumentException();
        return plugin.getDefaultBiomeProvider(name(), generator().id());
    }

    @SuppressWarnings("deprecation")
    public static WorldImage of(World world) {
        return new WorldImage(
                world.getName(),
                null, null,
                world.getEnvironment(),
                Objects.requireNonNullElse(world.getWorldType(), WorldType.NORMAL),
                world.canGenerateStructures(),
                world.isHardcore(),
                true,
                world.getSeed()
        );
    }

    @Nullable
    public static WorldImage of(File file) {
        return file.isFile() ? new GsonFile<WorldImage>(file, WorldImage.class).getRoot() : null;
    }
}
