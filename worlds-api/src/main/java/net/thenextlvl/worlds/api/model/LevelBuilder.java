package net.thenextlvl.worlds.api.model;

import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

@NullMarked
public interface LevelBuilder {
    @Nullable
    Boolean hardcore();

    @Nullable
    Boolean structures();

    @Nullable
    Boolean bonusChest();

    @Nullable
    Generator generator();

    @Nullable
    Long seed();

    @Nullable
    NamespacedKey key();

    @Nullable
    Preset preset();

    @Nullable
    String name();

    @Nullable Environment environment();

    @Nullable
    WorldPreset type();

    Path level();

    LevelBuilder environment(@Nullable Environment environment);

    LevelBuilder generator(@Nullable Generator generator);

    LevelBuilder hardcore(@Nullable Boolean hardcore);

    LevelBuilder key(@Nullable NamespacedKey key);

    LevelBuilder name(@Nullable String name);

    LevelBuilder preset(@Nullable Preset preset);

    LevelBuilder seed(@Nullable Long seed);

    LevelBuilder structures(@Nullable Boolean structures);

    LevelBuilder bonusChest(@Nullable Boolean bonusChest);

    LevelBuilder type(@Nullable WorldPreset type);

    Level build();
}
