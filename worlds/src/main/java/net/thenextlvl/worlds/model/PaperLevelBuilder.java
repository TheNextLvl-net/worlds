package net.thenextlvl.worlds.model;

import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.model.Generator;
import net.thenextlvl.worlds.api.model.Level;
import net.thenextlvl.worlds.api.model.LevelBuilder;
import net.thenextlvl.worlds.api.model.WorldPreset;
import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;

@NullMarked
public class PaperLevelBuilder implements LevelBuilder {
    private final WorldsPlugin plugin;
    private final File level;

    private @Nullable Boolean hardcore;
    private @Nullable Boolean structures;
    private @Nullable Boolean bonusChest;
    private @Nullable Generator generator;
    private @Nullable Long seed;
    private @Nullable NamespacedKey key;
    private @Nullable Preset preset;
    private @Nullable String name;
    private World.@Nullable Environment environment;
    private @Nullable WorldPreset type;

    public PaperLevelBuilder(WorldsPlugin plugin, File level) {
        this.plugin = plugin;
        this.level = level;
    }

    @Override
    public @Nullable Boolean hardcore() {
        return hardcore;
    }

    @Override
    public @Nullable Boolean structures() {
        return structures;
    }

    @Override
    public @Nullable Boolean bonusChest() {
        return bonusChest;
    }

    @Override
    public @Nullable Generator generator() {
        return generator;
    }

    @Override
    public @Nullable Long seed() {
        return seed;
    }

    @Override
    public @Nullable NamespacedKey key() {
        return key;
    }

    @Override
    public @Nullable Preset preset() {
        return preset;
    }

    @Override
    public @Nullable String name() {
        return name;
    }

    @Override
    public World.@Nullable Environment environment() {
        return environment;
    }

    @Override
    public @Nullable WorldPreset type() {
        return type;
    }

    @Override
    public File level() {
        return level;
    }

    @Override
    public LevelBuilder environment(World.@Nullable Environment environment) {
        this.environment = environment;
        return this;
    }

    @Override
    public LevelBuilder generator(@Nullable Generator generator) {
        this.generator = generator;
        return this;
    }

    @Override
    public LevelBuilder hardcore(@Nullable Boolean hardcore) {
        this.hardcore = hardcore;
        return this;
    }

    @Override
    public LevelBuilder key(@Nullable NamespacedKey key) {
        this.key = key;
        return this;
    }

    @Override
    public LevelBuilder name(@Nullable String name) {
        this.name = name;
        return this;
    }

    @Override
    public LevelBuilder preset(@Nullable Preset preset) {
        this.preset = preset;
        return this;
    }

    @Override
    public LevelBuilder seed(@Nullable Long seed) {
        this.seed = seed;
        return this;
    }

    @Override
    public LevelBuilder structures(@Nullable Boolean structures) {
        this.structures = structures;
        return this;
    }

    @Override
    public LevelBuilder bonusChest(@Nullable Boolean bonusChest) {
        this.bonusChest = bonusChest;
        return this;
    }

    @Override
    public LevelBuilder type(@Nullable WorldPreset type) {
        this.type = type;
        return this;
    }

    @Override
    public Level build() {
        return plugin.isRunningFolia() ? new FoliaLevel(plugin, this) : new PaperLevel(plugin, this);
    }
}
