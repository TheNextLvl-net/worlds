package net.thenextlvl.worlds.view;

import core.io.IO;
import core.io.PathIO;
import core.nbt.file.NBTFile;
import core.nbt.tag.*;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.model.LevelExtras;
import net.thenextlvl.worlds.preset.Biome;
import net.thenextlvl.worlds.preset.Layer;
import net.thenextlvl.worlds.preset.Preset;
import net.thenextlvl.worlds.preset.Structure;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class LevelView {
    private final WorldsPlugin plugin;

    public Stream<File> listLevels() {
        return Optional.ofNullable(plugin.getServer().getWorldContainer()
                        .listFiles(File::isDirectory)).stream()
                .flatMap(files -> Arrays.stream(files).filter(level ->
                        new File(level, "level.dat").isFile() ||
                        new File(level, "level.dat_old").isFile()));
    }

    public Stream<File> listOverworldLevels() {
        return listLevels()
                .filter(level -> !new File(level, "DIM1").exists())
                .filter(level -> !new File(level, "DIM-1").exists());
    }

    public boolean canLoad(File level) {
        return plugin.getServer().getWorlds().stream()
                .map(World::getWorldFolder)
                .noneMatch(level::equals);
    }

    public Stream<File> listNetherLevels() {
        return listLevels().filter(level -> new File(level, "DIM-1").exists());
    }

    public Stream<File> listEndLevels() {
        return listLevels().filter(level -> new File(level, "DIM1").exists());
    }

    public @Nullable World loadOverworldLevel(File level) {
        return loadLevel(level, World.Environment.NORMAL);
    }

    public @Nullable World loadNetherLevel(File level) {
        return loadLevel(level, World.Environment.NETHER);
    }

    public @Nullable World loadEndLevel(File level) {
        return loadLevel(level, World.Environment.THE_END);
    }

    private @Nullable World loadLevel(File level, World.Environment environment) {
        var data = getLevelDataFile(level).getRoot().<CompoundTag>optional("Data");
        var extras = data.flatMap(this::getExtras);

        if (extras.filter(LevelExtras::enabled).isEmpty()) return null;

        var settings = data.flatMap(tag -> tag.<CompoundTag>optional("WorldGenSettings"));
        var dimensions = settings.flatMap(tag -> tag.<CompoundTag>optional("dimensions"));
        var dimension = dimensions.flatMap(tag -> tag.<CompoundTag>optional(getDimension(tag, environment)));
        var generator = dimension.flatMap(tag -> tag.<CompoundTag>optional("generator"));

        var type = generator.flatMap(this::getWorldType);

        var generatorSettings = type.filter(worldType -> worldType.equals(WorldType.FLAT))
                .flatMap(worldType -> generator.flatMap(this::getSettings));

        var hardcore = data.flatMap(tag -> tag.<ByteTag>optional("hardcore"))
                .orElseThrow(() -> new NoSuchElementException("hardcore"))
                .getAsBoolean();
        var seed = settings.flatMap(tag -> tag.<IntTag>optional("seed"))
                .orElseThrow(() -> new NoSuchElementException("seed"))
                .getAsInt();
        var structures = settings.flatMap(tag -> tag.<ByteTag>optional("generate_features"))
                .orElseThrow(() -> new NoSuchElementException("generate_features"))
                .getAsBoolean();

        var key = extras.map(LevelExtras::key).orElseGet(() -> {
            var namespace = level.getName().toLowerCase().replace(" ", "_");
            return new NamespacedKey("worlds", namespace);
        });

        var creator = new WorldCreator(level.getName(), key)
                .environment(environment)
                .generateStructures(structures)
                .hardcore(hardcore)
                .seed(seed)
                .type(type.orElse(WorldType.NORMAL));

        generatorSettings.ifPresent(preset -> creator.generator(preset.serialize().toString()));


        var world = creator.createWorld();
        if (world == null) return null;

        extras.map(LevelExtras::autoSave).ifPresent(world::setAutoSave);

        return world;
    }

    public Optional<LevelExtras> getExtras(CompoundTag data) {
        return data.optional("BukkitValues")
                .map(Tag::getAsCompound)
                .map(values -> {
                    var key = values.optional("worlds:world_key")
                            .map(Tag::getAsString)
                            .map(NamespacedKey::fromString)
                            .orElse(null);
                    var autoSave = values.optional("worlds:auto_save")
                            .map(Tag::getAsBoolean)
                            .orElse(true);
                    var enabled = values.optional("worlds:enabled")
                            .map(Tag::getAsBoolean)
                            .orElse(true);
                    return new LevelExtras(key, autoSave, enabled);
                });
    }

    public Optional<Preset> getSettings(CompoundTag generator) {
        var settings = generator.<CompoundTag>optional("settings");

        if (settings.isEmpty()) return Optional.empty();

        var preset = new Preset();

        settings.flatMap(tag -> tag.<Tag>optional("biome"))
                .map(Tag::getAsString)
                .map(Biome::literal)
                .ifPresent(preset::biome);

        settings.flatMap(tag -> tag.<Tag>optional("features"))
                .map(Tag::getAsBoolean)
                .ifPresent(preset::features);

        settings.flatMap(tag -> tag.<Tag>optional("lakes"))
                .map(Tag::getAsBoolean)
                .ifPresent(preset::lakes);

        settings.flatMap(tag -> tag.<CompoundTag>optional("layers"))
                .map(tag -> tag.<CompoundTag>getAsList().stream().map(layer -> {
                    var block = layer.optional("block").orElseThrow().getAsString();
                    var height = layer.optional("height").orElseThrow().getAsInt();
                    return new Layer(block, height);
                }).collect(Collectors.toSet()))
                .ifPresent(preset::layers);

        settings.flatMap(tag -> tag.<CompoundTag>optional("structure_overrides"))
                .map(tag -> tag.<StringTag>getAsList().stream()
                        .map(structure -> new Structure(structure.getAsString()))
                        .collect(Collectors.toSet()))
                .ifPresent(preset::structures);

        return Optional.of(preset);
    }

    public NBTFile<CompoundTag> getLevelDataFile(File level) {
        return new NBTFile<>(Optional.of(
                IO.of(level, "level.dat")
        ).filter(PathIO::exists).orElseGet(() ->
                IO.of(level, "level.dat_old")
        ), new CompoundTag());
    }

    public String getDimension(CompoundTag dimensions, World.Environment environment) {
        return switch (environment) {
            case NORMAL -> "minecraft:overworld";
            case NETHER -> "minecraft:the_nether";
            case THE_END -> "minecraft:the_end";
            case CUSTOM -> dimensions.keySet().stream().filter(s -> !s.startsWith("minecraft")).findAny()
                    .orElseThrow(() -> new UnsupportedOperationException("Could not find custom dimension"));
        };
    }

    public Optional<WorldType> getWorldType(CompoundTag generator) {
        return getGeneratorType(generator).map(string -> switch (string) {
            case "minecraft:noise" -> WorldType.NORMAL;
            case "minecraft:flat" -> WorldType.FLAT;
            case "minecraft:debug" -> throw new IllegalArgumentException("Debug worlds are not yet supported");
            default -> throw new IllegalArgumentException("Unexpected generator type: " + string);
        });
    }

    public Optional<String> getGeneratorType(CompoundTag generator) {
        return generator.optional("type").map(Tag::getAsString);
    }
}
