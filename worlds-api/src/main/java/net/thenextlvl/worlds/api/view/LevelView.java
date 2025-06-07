package net.thenextlvl.worlds.api.view;

import core.nbt.tag.CompoundTag;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

@NullMarked
public interface LevelView {
    Optional<Level> read(Path directory);

    Optional<Preset> getFlatPreset(CompoundTag generator);

    Optional<String> getGenerator(World world);

    @Unmodifiable
    Set<Path> listLevels();

    boolean canLoad(Path level);

    boolean hasEndDimension(Path level);

    boolean hasNetherDimension(Path level);

    boolean isLevel(Path path);

    boolean unload(World world, boolean save);

    void save(World world, boolean flush);

    void saveLevelData(World world, boolean async);
}
