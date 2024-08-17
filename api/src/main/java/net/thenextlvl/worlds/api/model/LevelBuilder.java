package net.thenextlvl.worlds.api.model;

import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface LevelBuilder {
    @Nullable Boolean hardcore();

    @Nullable Boolean structures();

    @Nullable Generator generator();

    @Nullable Long seed();

    @Nullable NamespacedKey key();

    @Nullable Preset preset();

    @Nullable String name();

    @Nullable World.Environment environment();

    @Nullable WorldPreset type();

    File level();

    LevelBuilder environment(@Nullable World.Environment environment);

    LevelBuilder generator(@Nullable Generator generator);

    LevelBuilder hardcore(@Nullable Boolean hardcore);

    LevelBuilder key(@Nullable NamespacedKey key);

    LevelBuilder name(@Nullable String name);

    LevelBuilder preset(@Nullable Preset preset);

    LevelBuilder seed(@Nullable Long seed);

    LevelBuilder structures(@Nullable Boolean structures);

    LevelBuilder type(@Nullable WorldPreset type);

    Level build();
}
