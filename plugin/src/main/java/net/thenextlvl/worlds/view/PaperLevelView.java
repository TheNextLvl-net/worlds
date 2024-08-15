package net.thenextlvl.worlds.view;

import core.io.IO;
import core.io.PathIO;
import core.nbt.file.NBTFile;
import core.nbt.tag.*;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.model.LevelExtras;
import net.thenextlvl.worlds.api.model.WorldPreset;
import net.thenextlvl.worlds.api.preset.Biome;
import net.thenextlvl.worlds.api.preset.Layer;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.api.preset.Structure;
import net.thenextlvl.worlds.api.view.LevelView;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class PaperLevelView implements LevelView {
    private final WorldsPlugin plugin;

    @Override
    public Stream<File> listLevels() {
        return Optional.ofNullable(plugin.getServer().getWorldContainer()
                        .listFiles(File::isDirectory)).stream()
                .flatMap(files -> Arrays.stream(files).filter(this::isLevel));
    }

    @Override
    public boolean isLevel(File file) {
        return file.isDirectory() && (new File(file, "level.dat").isFile() || new File(file, "level.dat_old").isFile());
    }

    @Override
    public boolean hasNetherDimension(File level) {
        return new File(level, "DIM-1").isDirectory();
    }

    @Override
    public boolean hasEndDimension(File level) {
        return new File(level, "DIM1").isDirectory();
    }

    @Override
    public boolean canLoad(File level) {
        return plugin.getServer().getWorlds().stream()
                .map(World::getWorldFolder)
                .noneMatch(level::equals);
    }

    @Override
    public World.Environment getEnvironment(File level) {
        var end = hasEndDimension(level);
        var nether = hasNetherDimension(level);
        if (end && nether) return World.Environment.NORMAL;
        if (end) return World.Environment.THE_END;
        if (nether) return World.Environment.NETHER;
        return World.Environment.NORMAL;
    }

    @Override
    public @Nullable World loadLevel(File level) {
        return loadLevel(level, getEnvironment(level));
    }

    @Override
    public @Nullable World loadLevel(File level, @Nullable NamespacedKey key, Predicate<Optional<LevelExtras>> predicate) {
        return loadLevel(level, getEnvironment(level), key, predicate);
    }

    @Override
    public @Nullable World loadLevel(File level, World.Environment environment) {
        return loadLevel(level, environment, extras -> extras.map(LevelExtras::enabled).isPresent());
    }

    @Override
    public @Nullable World loadLevel(File level, Predicate<Optional<LevelExtras>> predicate) {
        return loadLevel(level, getEnvironment(level), predicate);
    }

    @Override
    public @Nullable World loadLevel(File level, World.Environment environment, Predicate<Optional<LevelExtras>> predicate) {
        return loadLevel(level, environment, null, predicate);
    }

    @Override
    public @Nullable World loadLevel(File level, World.Environment environment, @Nullable NamespacedKey key, Predicate<Optional<LevelExtras>> predicate) {
        var data = getLevelDataFile(level).getRoot().<CompoundTag>optional("Data");
        var extras = data.flatMap(this::getExtras);

        if (!predicate.test(extras)) return null;

        var settings = data.flatMap(tag -> tag.<CompoundTag>optional("WorldGenSettings"));
        var dimensions = settings.flatMap(tag -> tag.<CompoundTag>optional("dimensions"));
        var dimension = dimensions.flatMap(tag -> tag.<CompoundTag>optional(getDimension(tag, environment)));
        var generator = dimension.flatMap(tag -> tag.<CompoundTag>optional("generator"));

        var worldPreset = generator.flatMap(this::getWorldPreset);

        var generatorSettings = worldPreset.filter(preset -> preset.equals(WorldPreset.FLAT))
                .flatMap(worldType -> generator.flatMap(this::getFlatPreset));

        var hardcore = data.flatMap(tag -> tag.<ByteTag>optional("hardcore"))
                .orElseThrow(() -> new NoSuchElementException("hardcore"))
                .getAsBoolean();
        var seed = settings.flatMap(tag -> tag.<LongTag>optional("seed"))
                .orElseThrow(() -> new NoSuchElementException("seed"))
                .getAsInt();
        var structures = settings.flatMap(tag -> tag.<ByteTag>optional("generate_features"))
                .orElseThrow(() -> new NoSuchElementException("generate_features"))
                .getAsBoolean();

        var worldKey = Optional.ofNullable(key).orElseGet(() ->
                extras.map(LevelExtras::key).orElseGet(() -> {
                    var namespace = level.getName().toLowerCase()
                            .replace("(", "").replace(")", "")
                            .replace(" ", "_");
                    return new NamespacedKey("worlds", namespace);
                }));

        var creator = new WorldCreator(level.getName(), worldKey)
                .environment(environment)
                .generateStructures(structures)
                .hardcore(hardcore)
                .seed(seed)
                .type(typeOf(worldPreset.orElse(WorldPreset.NORMAL)));

        generatorSettings.ifPresent(preset -> creator.generatorSettings(preset.serialize().toString()));

        return creator.createWorld();
    }

    private WorldType typeOf(WorldPreset worldPreset) {
        if (worldPreset.equals(WorldPreset.AMPLIFIED)) return WorldType.AMPLIFIED;
        if (worldPreset.equals(WorldPreset.FLAT)) return WorldType.FLAT;
        if (worldPreset.equals(WorldPreset.LARGE_BIOMES)) return WorldType.LARGE_BIOMES;
        return WorldType.NORMAL;
    }

    @Override
    public Optional<LevelExtras> getExtras(CompoundTag data) {
        return data.optional("BukkitValues")
                .map(Tag::getAsCompound)
                .map(values -> {
                    var key = values.optional("worlds:world_key")
                            .map(Tag::getAsString)
                            .map(NamespacedKey::fromString)
                            .orElse(null);
                    var enabled = values.optional("worlds:enabled")
                            .map(Tag::getAsBoolean)
                            .orElse(true);
                    return new LevelExtras(key, enabled);
                });
    }

    @Override
    public Optional<Preset> getFlatPreset(CompoundTag generator) {
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

        settings.flatMap(tag -> tag.<ListTag<CompoundTag>>optional("layers"))
                .map(tag -> tag.stream().map(layer -> {
                    var block = layer.optional("block").orElseThrow().getAsString();
                    var height = layer.optional("height").orElseThrow().getAsInt();
                    return new Layer(block, height);
                }).collect(Collectors.toCollection(LinkedHashSet::new)))
                .ifPresent(preset::layers);

        settings.flatMap(tag -> tag.optional("structure_overrides")
                        .filter(Tag::isList).map(Tag::getAsList))
                .map(list -> list.stream()
                        .map(structure -> new Structure(structure.getAsString()))
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .ifPresent(preset::structures);
        settings.flatMap(tag -> tag.optional("structure_overrides")
                        .filter(Tag::isString).map(Tag::getAsString))
                .map(Structure::new)
                .ifPresent(preset::addStructure);

        return Optional.of(preset);
    }

    @Override
    public NBTFile<CompoundTag> getLevelDataFile(File level) {
        return new NBTFile<>(Optional.of(
                IO.of(level, "level.dat")
        ).filter(PathIO::exists).orElseGet(() ->
                IO.of(level, "level.dat_old")
        ), new CompoundTag());
    }

    @Override
    public String getDimension(CompoundTag dimensions, World.Environment environment) {
        return switch (environment) {
            case NORMAL -> "minecraft:overworld";
            case NETHER -> "minecraft:the_nether";
            case THE_END -> "minecraft:the_end";
            case CUSTOM -> dimensions.keySet().stream().filter(s -> !s.startsWith("minecraft")).findAny()
                    .orElseThrow(() -> new UnsupportedOperationException("Could not find custom dimension"));
        };
    }

    @Override
    public Optional<WorldPreset> getWorldPreset(CompoundTag generator) {

        var settings = getGeneratorSettings(generator);
        if (settings.filter(s -> s.equals(WorldPreset.LARGE_BIOMES.key().asString())).isPresent())
            return Optional.of(WorldPreset.LARGE_BIOMES);
        if (settings.filter(s -> s.equals(WorldPreset.AMPLIFIED.key().asString())).isPresent())
            return Optional.of(WorldPreset.AMPLIFIED);

        var type = generator.<CompoundTag>optional("biome_source")
                .flatMap(tag -> tag.<StringTag>optional("type"))
                .map(Tag::getAsString);

        if (type.filter(s -> s.equals(WorldPreset.SINGLE_BIOME.key().asString())).isPresent())
            return Optional.of(WorldPreset.SINGLE_BIOME);
        if (type.filter(s -> s.equals(WorldPreset.CHECKERBOARD.key().asString())).isPresent())
            return Optional.of(WorldPreset.CHECKERBOARD);

        var generatorType = getGeneratorType(generator);
        if (generatorType.filter(s -> s.equals(WorldPreset.DEBUG.key().asString())).isPresent())
            return Optional.of(WorldPreset.DEBUG);
        if (generatorType.filter(s -> s.equals(WorldPreset.FLAT.key().asString())).isPresent())
            return Optional.of(WorldPreset.FLAT);
        if (generatorType.filter(s -> s.equals(WorldPreset.NORMAL.key().asString())).isPresent())
            return Optional.of(WorldPreset.NORMAL);

        return Optional.empty();
    }

    @Override
    public Optional<String> getGeneratorSettings(CompoundTag generator) {
        return generator.optional("settings").filter(Tag::isString).map(Tag::getAsString);
    }

    @Override
    public Optional<String> getGeneratorType(CompoundTag generator) {
        return generator.optional("type").map(Tag::getAsString);
    }

    @Override
    public void saveLevel(World world, boolean flush) {
        var level = ((CraftWorld) world).getHandle();
        var oldSave = level.noSave;
        level.noSave = false;
        level.save(null, flush, false);
        level.noSave = oldSave;
    }

    @Override
    public void saveLevelData(World world, boolean async) {
        var level = ((CraftWorld) world).getHandle();
        if (level.getDragonFight() != null) {
            level.serverLevelData.setEndDragonFightData(level.getDragonFight().saveData());
        }
        level.getChunkSource().getDataStorage().save(async);

        level.serverLevelData.setWorldBorder(level.getWorldBorder().createSettings());
        level.serverLevelData.setCustomBossEvents(level.getServer().getCustomBossEvents().save(level.registryAccess()));
        level.convertable.saveDataTag(level.getServer().registryAccess(), level.serverLevelData, level.getServer().getPlayerList().getSingleplayerData());
    }
}
