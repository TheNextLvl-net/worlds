package net.thenextlvl.worlds.api.view;

import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import net.thenextlvl.worlds.api.model.LevelExtras;
import net.thenextlvl.worlds.api.model.WorldPreset;
import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface LevelView {
    @Nullable World loadLevel(File level);

    @Nullable World loadLevel(File level, Predicate<Optional<LevelExtras>> predicate);

    @Nullable World loadLevel(File level, World.Environment environment);

    @Nullable World loadLevel(File level, World.Environment environment, Predicate<Optional<LevelExtras>> predicate);

    NBTFile<CompoundTag> getLevelDataFile(File level);

    Optional<LevelExtras> getExtras(CompoundTag data);

    Optional<Preset> getFlatPreset(CompoundTag generator);

    Optional<String> getGeneratorSettings(CompoundTag generator);

    Optional<String> getGeneratorType(CompoundTag generator);

    Optional<WorldPreset> getWorldPreset(CompoundTag generator);

    Stream<File> listLevels();

    String getDimension(CompoundTag dimensions, World.Environment environment);

    World.Environment getEnvironment(File level);

    boolean canLoad(File level);

    boolean hasEndDimension(File level);

    boolean hasNetherDimension(File level);

    boolean isLevel(File file);

    void saveLevel(World world, boolean flush);

    void saveLevelData(World world, boolean async);
}
