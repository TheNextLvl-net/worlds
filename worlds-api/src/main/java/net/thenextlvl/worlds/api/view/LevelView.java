package net.thenextlvl.worlds.api.view;

import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import net.thenextlvl.worlds.api.model.LevelExtras;
import net.thenextlvl.worlds.api.model.WorldPreset;
import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

@NullMarked
public interface LevelView {
    NBTFile<CompoundTag> getLevelDataFile(Path level);

    Optional<LevelExtras> getExtras(CompoundTag data);

    Optional<Preset> getFlatPreset(CompoundTag generator);

    Optional<String> getGenerator(World world);

    Optional<String> getGeneratorSettings(CompoundTag generator);

    Optional<String> getGeneratorType(CompoundTag generator);

    Optional<WorldPreset> getWorldPreset(CompoundTag generator);

    @Unmodifiable
    Set<Path> listLevels();

    String getDimension(CompoundTag dimensions, World.Environment environment);

    World.Environment getEnvironment(Path level);

    boolean canLoad(Path level);

    boolean hasEndDimension(Path level);

    boolean hasNetherDimension(Path level);

    boolean isLevel(Path path);

    boolean unloadLevel(World world, boolean save);

    void saveLevel(World world, boolean flush);

    void saveLevelData(World world, boolean async);
}
