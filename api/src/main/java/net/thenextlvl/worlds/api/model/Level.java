package net.thenextlvl.worlds.api.model;

import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import net.kyori.adventure.key.Keyed;
import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@NullMarked
public interface Level extends Keyed {
    @Nullable
    Generator generator();

    @Nullable Preset preset();

    NBTFile<CompoundTag> levelData();

    NamespacedKey key();

    Optional<World> create();

    String name();

    World.Environment environment();

    WorldPreset type();

    boolean enabled();

    boolean hardcore();

    boolean importedBefore();

    boolean structures();

    long seed();
}
