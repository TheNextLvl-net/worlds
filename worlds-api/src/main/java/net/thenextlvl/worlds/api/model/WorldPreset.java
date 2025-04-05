package net.thenextlvl.worlds.api.model;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record WorldPreset(Key key) implements Keyed {
    public static final WorldPreset AMPLIFIED = new WorldPreset(Key.key("minecraft", "amplified"));
    public static final WorldPreset CHECKERBOARD = new WorldPreset(Key.key("minecraft", "checkerboard"));
    public static final WorldPreset DEBUG = new WorldPreset(Key.key("minecraft", "debug"));
    public static final WorldPreset FLAT = new WorldPreset(Key.key("minecraft", "flat"));
    public static final WorldPreset LARGE_BIOMES = new WorldPreset(Key.key("minecraft", "large_biomes"));
    public static final WorldPreset NORMAL = new WorldPreset(Key.key("minecraft", "noise"));
    public static final WorldPreset SINGLE_BIOME = new WorldPreset(Key.key("minecraft", "fixed"));
}
