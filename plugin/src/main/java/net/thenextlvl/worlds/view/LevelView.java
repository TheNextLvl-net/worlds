package net.thenextlvl.worlds.view;

import core.io.IO;
import core.io.PathIO;
import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.StringTag;
import core.nbt.tag.Tag;
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
        var root = new NBTFile<>(Optional.of(
                IO.of(level, "level.dat")
        ).filter(PathIO::exists).orElseGet(() ->
                IO.of(level, "level.dat_old")
        ), new CompoundTag()).getRoot();

        var data = root.getAsCompound("Data");
        var extras = readExtras(data);

        if (extras.filter(LevelExtras::enabled).isEmpty()) return null;

        var settings = data.getAsCompound("WorldGenSettings");
        var dimensions = settings.getAsCompound("dimensions");

        var dimension = dimensions.optional(switch (environment) {
            case NORMAL -> "minecraft:overworld";
            case NETHER -> "minecraft:the_nether";
            case THE_END -> "minecraft:the_end";
            case CUSTOM -> throw new UnsupportedOperationException("Custom dimensions are not yet supported");
        }).orElseThrow().getAsCompound();

        var generator = dimension.getAsCompound("generator");

        var type = generator.optional("type")
                .map(Tag::getAsString)
                .map(string -> switch (string) {
                    case "minecraft:noise" -> WorldType.NORMAL;
                    case "minecraft:flat" -> WorldType.FLAT;
                    case "minecraft:debug" -> throw new IllegalArgumentException("Debug worlds are not yet supported");
                    default -> throw new IllegalArgumentException("Unexpected generator type: " + string);
                }).orElseThrow(() -> new NoSuchElementException("type"));

        var generatorSettings = type.equals(WorldType.FLAT) ? readSettings(generator) : null;

        var hardcore = data.optional("hardcore")
                .orElseThrow(() -> new NoSuchElementException("hardcore"))
                .getAsBoolean();
        var seed = settings.optional("seed")
                .orElseThrow(() -> new NoSuchElementException("seed"))
                .getAsInt();
        var structures = settings.optional("generate_features")
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
                .type(type);

        if (generatorSettings != null) creator.generatorSettings(
                Preset.serialize(generatorSettings).toString()
        );


        var world = creator.createWorld();
        if (world == null) return null;

        extras.map(LevelExtras::autoSave).ifPresent(world::setAutoSave);

        return world;
    }

    private Optional<LevelExtras> readExtras(CompoundTag data) {
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

    private Preset readSettings(CompoundTag generator) {
        var settings = generator.getAsCompound("settings");

        var biome = settings.optional("biome")
                .orElseThrow(() -> new NoSuchElementException("biome"))
                .getAsString();
        var features = settings.optional("features")
                .orElseThrow(() -> new NoSuchElementException("features"))
                .getAsBoolean();
        var lakes = settings.optional("lakes")
                .orElseThrow(() -> new NoSuchElementException("lakes"))
                .getAsBoolean();

        var layers = settings.optional("layers")
                .orElseThrow(() -> new NoSuchElementException("layers"))
                .<CompoundTag>getAsList().stream()
                .map(layer -> {
                    var block = layer.optional("block").orElseThrow().getAsString();
                    var height = layer.optional("height").orElseThrow().getAsInt();
                    return new Layer(block, height);
                }).collect(Collectors.toSet());

        var structures = settings.optional("structure_overrides")
                .orElseThrow(() -> new NoSuchElementException("structure_overrides"))
                .<StringTag>getAsList().stream()
                .map(structure -> new Structure(structure.getAsString()))
                .collect(Collectors.toSet());

        return new Preset()
                .biome(Biome.literal(biome))
                .features(features)
                .lakes(lakes)
                .layers(layers)
                .structures(structures);
    }
}
