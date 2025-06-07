package net.thenextlvl.worlds.view;

import core.io.IO;
import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.ListTag;
import core.nbt.tag.Tag;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import net.minecraft.server.level.ServerLevel;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.preset.Biome;
import net.thenextlvl.worlds.api.preset.Layer;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.api.preset.Structure;
import net.thenextlvl.worlds.api.view.LevelView;
import net.thenextlvl.worlds.level.LevelData;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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

    public Optional<Path> getLevelDataPath(Path level) {
        return Optional.ofNullable(getFile(level, "level.dat"))
                .or(() -> Optional.ofNullable(getFile(level, "level.dat_old")));
    }

    public Optional<NBTFile<CompoundTag>> getLevelDataFile(Path level) {
        return getLevelDataPath(level).map(path -> new NBTFile<>(IO.of(path), new CompoundTag()));
    }

    private static @Nullable Path getFile(Path level, String other) {
        var resolved = level.resolve(other);
        return Files.exists(resolved) ? resolved : null;
    }

    @Override
    public Optional<Level> read(Path directory) {
        return LevelData.read(plugin, directory);
    }

    @Override
    @SuppressWarnings("PatternValidation")
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
                        .map(Tag::getAsString)
                        .map(Structure::new)
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .ifPresent(preset::structures);
        settings.flatMap(tag -> tag.optional("structure_overrides")
                        .filter(Tag::isString).map(Tag::getAsString))
                .map(Structure::new)
                .ifPresent(preset::addStructure);

        return Optional.of(preset);
    }

    public Optional<JavaPlugin> getGenerator(World world) {
        return Optional.ofNullable(world.getGenerator())
                .map(chunkGenerator -> chunkGenerator.getClass().getClassLoader())
                .filter(ConfiguredPluginClassLoader.class::isInstance)
                .map(ConfiguredPluginClassLoader.class::cast)
                .map(ConfiguredPluginClassLoader::getPlugin);
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
    public boolean unload(World world, boolean save) {
        return plugin.getServer().unloadWorld(world, save);
    }

    /**
     * @see CraftWorld#save(boolean)
     */
    @Override
    public void save(World world, boolean flush) {
        var level = ((CraftWorld) world).getHandle();
        var oldSave = level.noSave;
        level.noSave = false;
        level.save(null, flush, false);
        level.noSave = oldSave;
    }

    /**
     * @see ServerLevel#saveIncrementally(boolean)
     * @see ServerLevel#saveLevelData(boolean)
     */
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
