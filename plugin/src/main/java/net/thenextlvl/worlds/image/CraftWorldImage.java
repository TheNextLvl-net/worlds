package net.thenextlvl.worlds.image;

import com.google.gson.JsonObject;
import core.io.IO;
import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.*;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@Getter
@Setter
@NoArgsConstructor
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
    private boolean generateStructures;
    private boolean hardcore;
    private boolean loadOnStart;
    private long seed;

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
        var file = new NBTFile<>(dataFile, new CompoundTag());
        var data = file.getRoot().getAsCompound("Data");
        var worldGenSettings = data.getAsCompound("WorldGenSettings");
        worldGenSettings.add("seed", seed());
        worldGenSettings.add("generate_features", generateStructures());
        data.add("hardcore", hardcore());
        file.save();
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
