package net.thenextlvl.worlds.image;

import core.file.format.GsonFile;
import core.io.IO;
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
public class WorldImage implements Cloneable {
    private String name;
    private @Nullable String settings;
    private @Nullable Generator generator;
    private @Nullable DeletionType deletion;
    private World.Environment environment;
    private WorldType type;
    private boolean autoSave;
    private boolean generateStructures;
    private boolean hardcore;
    private boolean loadOnStart;
    private long seed;

    public @Nullable World build() {
        var creator = new WorldCreator(name, new NamespacedKey("worlds", name.toLowerCase().replace(" ", "_")))
                .generator(resolveChunkGenerator())
                .biomeProvider(resolveBiomeProvider())
                .generateStructures(generateStructures())
                .generatorSettings(settings() != null ? settings() : "")
                .environment(environment())
                .hardcore(hardcore())
                .type(type())
                .seed(seed());
        var world = creator.createWorld();
        if (world != null) world.setAutoSave(autoSave());
        return revalidate(world);
    }

    public @Nullable World revalidate(@Nullable World world) {
        if (world == null) return null;
        this.generateStructures = world.canGenerateStructures();
        this.hardcore = world.isHardcore();
        this.seed = world.getSeed();
        return world;
    }

    private @Nullable ChunkGenerator resolveChunkGenerator() {
        if (generator() == null) return null;
        var plugin = Bukkit.getPluginManager().getPlugin(generator().plugin());
        if (plugin == null || !plugin.isEnabled()) throw new IllegalArgumentException();
        return plugin.getDefaultWorldGenerator(name(), generator().id());
    }

    private @Nullable BiomeProvider resolveBiomeProvider() {
        if (generator() == null) return null;
        var plugin = Bukkit.getPluginManager().getPlugin(generator().plugin());
        if (plugin == null || !plugin.isEnabled()) throw new IllegalArgumentException();
        return plugin.getDefaultBiomeProvider(name(), generator().id());
    }

    @SuppressWarnings("deprecation")
    public static WorldImage of(World world) {
        return new WorldImage(
                world.getName(),
                null, null, null,
                world.getEnvironment(),
                Objects.requireNonNullElse(world.getWorldType(), WorldType.NORMAL),
                world.isAutoSave(),
                world.canGenerateStructures(),
                world.isHardcore(),
                true,
                world.getSeed()
        );
    }

    public static @Nullable WorldImage of(File file) {
        return file.isFile() ? new GsonFile<WorldImage>(IO.of(file), WorldImage.class).getRoot() : null;
    }

    @Override
    public WorldImage clone() {
        try {
            return (WorldImage) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
