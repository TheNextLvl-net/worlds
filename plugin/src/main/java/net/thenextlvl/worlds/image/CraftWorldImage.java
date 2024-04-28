package net.thenextlvl.worlds.image;

import com.google.gson.JsonObject;
import core.io.IO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.thenextlvl.worlds.util.WorldReader;
import org.bukkit.*;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@Getter
@Setter
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
public class CraftWorldImage implements WorldImage {
    private String name;
    private NamespacedKey key;
    private @Nullable JsonObject settings;
    private @Nullable Generator generator;
    private @Nullable DeletionType deletionType;
    private World.Environment environment;
    private WorldType worldType;
    private boolean autoSave;
    private boolean loadOnStart;

    private volatile boolean generateStructures;
    private volatile boolean hardcore;
    private volatile long seed;

    private volatile boolean skipValidation;

    public CraftWorldImage(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Override
    public @Nullable World build() {
        try {
            preValidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        var creator = new WorldCreator(name(), key())
                .generator(resolveChunkGenerator())
                .biomeProvider(resolveBiomeProvider())
                .generateStructures(generateStructures())
                .generatorSettings(settings() != null ? settings().toString() : "")
                .environment(environment())
                .hardcore(hardcore())
                .type(worldType())
                .seed(seed());
        var world = creator.createWorld();
        if (world != null) world.setAutoSave(autoSave());
        return world;
    }

    private void preValidate() {
        var worldFolder = new File(Bukkit.getWorldContainer(), name());
        var dataFile = IO.of(worldFolder, "level.dat");
        if (!dataFile.exists()) return;
        var reader = new WorldReader(dataFile);
        if (!skipValidation) {
            reader.generateStructures().ifPresent(this::generateStructures);
            reader.hardcore().ifPresent(this::hardcore);
            reader.seed().ifPresent(this::seed);
        }
        var data = reader.file().getRoot().getAsCompound("Data");
        var worldGenSettings = data.getAsCompound("WorldGenSettings");
        worldGenSettings.add("seed", seed());
        worldGenSettings.add("generate_features", generateStructures());
        data.add("hardcore", hardcore());
        reader.file().save();
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
}
