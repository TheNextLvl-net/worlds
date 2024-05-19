package net.thenextlvl.worlds.util;

import core.annotation.TypesAreNotNullByDefault;
import core.io.IO;
import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Optional;
import java.util.OptionalLong;

@Getter
@Accessors(fluent = true)
@TypesAreNotNullByDefault
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class WorldReader {
    private final NBTFile<CompoundTag> file;
    private OptionalLong seed = OptionalLong.empty();
    private Optional<Boolean> generateStructures = Optional.empty();
    private Optional<Boolean> hardcore = Optional.empty();

    public WorldReader(IO dataFile) {
        this.file = new NBTFile<>(dataFile, new CompoundTag());

        if (!file.getRoot().containsKey("Data")) return;
        var data = file.getRoot().getAsCompound("Data");

        var oldSeed = data.get("RandomSeed");
        if (oldSeed != null) this.seed = OptionalLong.of(oldSeed.getAsLong());

        var oldStructures = data.get("MapFeatures");
        if (oldStructures != null) this.seed = OptionalLong.of(oldStructures.getAsLong());

        var hardcore = data.get("hardcore");
        if (hardcore != null) this.hardcore = Optional.of(hardcore.getAsBoolean());

        if (!data.containsKey("WorldGenSettings")) return;
        var worldGenSettings = data.getAsCompound("WorldGenSettings");

        var seed = worldGenSettings.get("seed");
        if (seed != null) this.seed = OptionalLong.of(seed.getAsLong());

        var structures = worldGenSettings.get("generate_features");
        if (structures != null) generateStructures = Optional.of(structures.getAsBoolean());
    }

    public WorldReader(String name) {
        this(IO.of(new File(Bukkit.getWorldContainer(), name), "level.dat"));
    }
}
