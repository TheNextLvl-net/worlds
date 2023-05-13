package net.thenextlvl.worlds.image;

import core.api.file.format.GsonFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.*;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class WorldImage {
    private String name;
    private @Nullable String settings;
    private @Nullable Generator generator;
    private DeletionType deletion;
    private World.Environment environment;
    private WorldType type;
    private boolean generateStructures;
    private boolean hardcore;
    private boolean loadOnStart;
    private long seed;

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
                DeletionType.NONE,
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
