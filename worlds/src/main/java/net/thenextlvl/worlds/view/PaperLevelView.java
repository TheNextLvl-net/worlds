package net.thenextlvl.worlds.view;

import core.io.IO;
import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import net.minecraft.server.level.ServerLevel;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.view.LevelView;
import net.thenextlvl.worlds.level.LevelData;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public Optional<Level.Builder> read(Path directory) {
        return LevelData.read(plugin, directory);
    }

    @Override
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
        if (!plugin.getServer().unloadWorld(world, save)) return false;
        var dragonBattle = world.getEnderDragonBattle();
        if (dragonBattle != null) dragonBattle.getBossBar().removeAll();
        return true;
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
