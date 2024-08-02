package net.thenextlvl.worlds.view;

import core.io.IO;
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
import org.bukkit.*;
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
                        new File(level, "level.dat").isFile()));
    }

    public Stream<File> listOverworldLevels() {
        return listLevels()
                .filter(level -> !new File(level, "DIM1").exists())
                .filter(level -> !new File(level, "DIM-1").exists());
    }

    public boolean canLoad(File level) {
        return Bukkit.getWorld(level.getName()) == null;
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
        var root = new NBTFile<>(IO.of(level, "level.dat"), new CompoundTag()).getRoot();

        var data = root.getAsCompound("Data");
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

        var extras = readExtras(root);

        var namespace = level.getName().toLowerCase().replace(" ", "_");
        var creator = new WorldCreator(level.getName(), new NamespacedKey("worlds", namespace))
                .environment(environment)
                .generateStructures(structures)
                .hardcore(hardcore)
                .seed(seed)
                .type(type);

        if (generatorSettings != null) creator.generatorSettings(
                Preset.serialize(generatorSettings).toString()
        );

        return creator.createWorld();
    }

    private LevelExtras readExtras(CompoundTag root) {
        return null;
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
