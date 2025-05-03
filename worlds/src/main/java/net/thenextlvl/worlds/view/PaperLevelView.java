package net.thenextlvl.worlds.view;

import core.io.IO;
import core.io.PathIO;
import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.ListTag;
import core.nbt.tag.StringTag;
import core.nbt.tag.Tag;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.model.Generator;
import net.thenextlvl.worlds.api.model.LevelExtras;
import net.thenextlvl.worlds.api.model.WorldPreset;
import net.thenextlvl.worlds.api.preset.Biome;
import net.thenextlvl.worlds.api.preset.Layer;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.api.preset.Structure;
import net.thenextlvl.worlds.api.view.LevelView;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@NullMarked
public class PaperLevelView implements LevelView {
    protected final WorldsPlugin plugin;

    public PaperLevelView(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public NBTFile<CompoundTag> getLevelDataFile(Path level) {
        return new NBTFile<>(Optional.of(
                IO.of(level.resolve("level.dat"))
        ).filter(PathIO::exists).orElseGet(() ->
                IO.of(level.resolve("level.dat_old"))
        ), new CompoundTag());
    }

    @Override
    public Optional<LevelExtras> getExtras(CompoundTag data) {
        return data.optional("BukkitValues").map(Tag::getAsCompound).map(values -> {
            var key = values.optional("worlds:world_key").map(Tag::getAsString).map(NamespacedKey::fromString);
            var generator = values.optional("worlds:generator").map(Tag::getAsString).map(serialized ->
                    Generator.deserialize(plugin, serialized));
            var enabled = values.optional("worlds:enabled").map(Tag::getAsBoolean);
            if (key.isEmpty() && generator.isEmpty() && enabled.isEmpty()) return null;
            return new LevelExtras(key.orElse(null), generator.orElse(null), enabled.orElse(false));
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
    public Optional<String> getGenerator(World world) {
        if (world.getGenerator() == null) return Optional.empty();
        var loader = world.getGenerator().getClass().getClassLoader();
        if (!(loader instanceof ConfiguredPluginClassLoader pluginLoader)) return Optional.empty();
        if (pluginLoader.getPlugin() == null) return Optional.empty();
        return Optional.of(pluginLoader.getPlugin().getName());
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
    public Set<Path> listLevels() {
        try (var stream = Files.list(plugin.getServer().getWorldContainer().toPath())) {
            return stream.filter(this::isLevel).collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            return Set.of();
        }
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
    public World.Environment getEnvironment(Path level) {
        var end = hasEndDimension(level);
        var nether = hasNetherDimension(level);
        if (end && nether) return World.Environment.NORMAL;
        if (end) return World.Environment.THE_END;
        if (nether) return World.Environment.NETHER;
        return World.Environment.NORMAL;
    }

    @Override
    public boolean canLoad(Path level) {
        return plugin.getServer().getWorlds().stream()
                .map(World::getWorldFolder)
                .map(File::toPath)
                .noneMatch(level::equals);
    }

    @Override
    public boolean hasEndDimension(Path level) {
        return Files.isDirectory(level.resolve("DIM1"));
    }

    @Override
    public boolean hasNetherDimension(Path level) {
        return Files.isDirectory(level.resolve("DIM-1"));
    }

    @Override
    public boolean isLevel(Path path) {
        return Files.isRegularFile(path.resolve("level.dat")) || Files.isRegularFile(path.resolve("level.dat_old"));
    }

    @Override
    public boolean unloadLevel(World world, boolean save) {
        return plugin.getServer().unloadWorld(world, save);
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
        var save = level.getChunkSource().getDataStorage().scheduleSave();
        if (!async) save.join();

        level.serverLevelData.setWorldBorder(level.getWorldBorder().createSettings());
        level.serverLevelData.setCustomBossEvents(level.getServer().getCustomBossEvents().save(level.registryAccess()));
        level.getChunkSource().getDataStorage().saveAndJoin();
    }
}
