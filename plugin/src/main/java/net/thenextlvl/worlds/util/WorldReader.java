package net.thenextlvl.worlds.util;

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
public class WorldReader {
    private final NBTFile<CompoundTag> file;
    private OptionalLong seed = OptionalLong.empty();
    private Optional<Boolean> generateStructures = Optional.empty();
    private Optional<Boolean> hardcore = Optional.empty();

    public WorldReader(IO dataFile) {
        this.file = new NBTFile<>(dataFile, new CompoundTag());
        if (!file.getRoot().containsKey("Data")) return;
        var data = file.getRoot().getAsCompound("Data");
        if (!data.containsKey("WorldGenSettings")) return;
        var worldGenSettings = data.getAsCompound("WorldGenSettings");
        if (worldGenSettings.containsKey("seed"))
            seed = OptionalLong.of(worldGenSettings.get("seed").getAsLong());
        if (worldGenSettings.containsKey("generate_features"))
            generateStructures = Optional.of(worldGenSettings.get("generate_features").getAsBoolean());
        if (worldGenSettings.containsKey("hardcore"))
            hardcore = Optional.of(data.get("hardcore").getAsBoolean());
    }

    public WorldReader(String name) {
        this(IO.of(new File(Bukkit.getWorldContainer(), name), "level.dat"));
    }
}
