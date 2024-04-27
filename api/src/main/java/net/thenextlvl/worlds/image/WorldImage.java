package net.thenextlvl.worlds.image;

import com.google.gson.GsonBuilder;
import core.file.FileIO;
import core.file.format.GsonFile;
import core.io.IO;
import core.paper.adapters.key.KeyAdapter;
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
    private NamespacedKey key;
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
        var creator = new WorldCreator(name, key)
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
                world.getKey(),
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
        return file.isFile() ? loadFile(IO.of(file), null).getRoot() : null;
    }

    public static FileIO<WorldImage> loadFile(IO file, @Nullable WorldImage root) {
        var gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(NamespacedKey.class, KeyAdapter.Bukkit.INSTANCE)
                .setPrettyPrinting()
                .create();
        return root != null ? new GsonFile<>(file, root, gson) : new GsonFile<>(file, WorldImage.class, gson);
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
