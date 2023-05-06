package net.thenextlvl.worlds.image;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record WorldImage(
        String name,
        @Nullable Generator generator,
        World.Environment environment,
        WorldType type,
        boolean generateStructures,
        boolean hardcore,
        long seed
) {

    @Nullable
    public World build() {
        var creator = WorldCreator.name(name)
                .generator(resolveChunkGenerator())
                .biomeProvider(resolveBiomeProvider())
                .generateStructures(generateStructures())
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

    public static WorldImage of(World world) {
        return of(world, null);
    }

    @SuppressWarnings("deprecation")
    public static WorldImage of(World world, @Nullable Generator generator) {
        return new WorldImage(
                world.getName(),
                generator,
                world.getEnvironment(),
                Objects.requireNonNullElse(world.getWorldType(), WorldType.NORMAL),
                world.canGenerateStructures(),
                world.isHardcore(),
                world.getSeed()
        );
    }
}
