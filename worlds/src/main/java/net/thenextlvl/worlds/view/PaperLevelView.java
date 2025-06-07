package net.thenextlvl.worlds.view;

import core.io.IO;
import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import net.kyori.adventure.key.Key;
import net.minecraft.server.level.ServerLevel;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.event.WorldActionScheduledEvent;
import net.thenextlvl.worlds.api.event.WorldActionScheduledEvent.ActionType;
import net.thenextlvl.worlds.api.event.WorldDeleteEvent;
import net.thenextlvl.worlds.api.event.WorldRegenerateEvent;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@NullMarked
public class PaperLevelView implements LevelView {
    private final Map<Key, Thread> regenerations = new HashMap<>();
    private final Map<Key, Thread> deletions = new HashMap<>();
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

    @Override
    public DeletionResult delete(World world, boolean schedule) {
        return schedule ? scheduleDeletion(world) : deleteNow(world);
    }

    @Override
    public boolean cancelScheduledDeletion(World world) {
        var thread = deletions.remove(world.key());
        return thread != null && Runtime.getRuntime().removeShutdownHook(thread);
    }

    @Override
    public boolean isDeletionScheduled(World world) {
        return deletions.containsKey(world.getKey());
    }

    @Override
    public DeletionResult regenerate(World world, boolean schedule) {
        return schedule ? scheduleRegeneration(world) : regenerateNow(world);
    }

    @Override
    public boolean cancelScheduledRegeneration(World world) {
        var thread = regenerations.remove(world.key());
        return thread != null && Runtime.getRuntime().removeShutdownHook(thread);
    }

    @Override
    public boolean isRegenerationScheduled(World world) {
        return regenerations.containsKey(world.getKey());
    }

    private DeletionResult deleteNow(World world) {
        if (plugin.isRunningFolia() || world.getKey().asString().equals("minecraft:overworld"))
            return DeletionResult.REQUIRES_SCHEDULING;

        if (!new WorldDeleteEvent(world).callEvent()) return DeletionResult.FAILED;

        var fallback = plugin.getServer().getWorlds().getFirst().getSpawnLocation();
        world.getPlayers().forEach(player -> player.teleport(fallback));

        if (!plugin.levelView().unload(world, false))
            return DeletionResult.UNLOAD_FAILED;

        delete(world.getWorldFolder().toPath());
        return DeletionResult.SUCCESS;
    }

    private DeletionResult scheduleDeletion(World world) {
        if (deletions.containsKey(world.getKey())) return DeletionResult.SCHEDULED;

        var event = new WorldActionScheduledEvent(world, ActionType.DELETE);
        if (!event.callEvent()) return DeletionResult.FAILED;

        var action = event.getAction() == null
                ? (Consumer<Path>) this::delete
                : event.getAction().andThen(this::delete);

        var path = world.getWorldFolder().toPath();
        var hook = new Thread(() -> action.accept(path), "world-deletion");

        Runtime.getRuntime().addShutdownHook(hook);
        deletions.put(world.getKey(), hook);
        return DeletionResult.SCHEDULED;
    }

    private DeletionResult regenerateNow(World world) {
        if (plugin.isRunningFolia() || world.getKey().asString().equals("minecraft:overworld"))
            return DeletionResult.REQUIRES_SCHEDULING;

        if (!new WorldRegenerateEvent(world).callEvent()) return DeletionResult.FAILED;

        var players = world.getPlayers();

        var fallback = plugin.getServer().getWorlds().getFirst().getSpawnLocation();
        players.forEach(player -> player.teleport(fallback, TeleportCause.PLUGIN));

        plugin.levelView().saveLevelData(world, false);
        if (!plugin.levelView().unload(world, false)) return DeletionResult.UNLOAD_FAILED;

        regenerate(world.getWorldFolder().toPath());

        var regenerated = plugin.levelBuilder(world).build().create().orElse(null);
        if (regenerated != null) players.forEach(player ->
                player.teleportAsync(regenerated.getSpawnLocation(), TeleportCause.PLUGIN));
        return regenerated != null ? DeletionResult.SUCCESS : DeletionResult.FAILED;
    }

    private DeletionResult scheduleRegeneration(World world) {
        if (regenerations.containsKey(world.getKey())) return DeletionResult.SCHEDULED;

        var event = new WorldActionScheduledEvent(world, ActionType.REGENERATE);
        if (!event.callEvent()) return DeletionResult.FAILED;

        var action = event.getAction() == null
                ? (Consumer<Path>) this::regenerate
                : event.getAction().andThen(this::regenerate);

        var path = world.getWorldFolder().toPath();
        var hook = new Thread(() -> action.accept(path), "world-regeneration");

        Runtime.getRuntime().addShutdownHook(hook);
        regenerations.put(world.getKey(), hook);
        return DeletionResult.SCHEDULED;
    }

    private void regenerate(Path level) {
        delete(level.resolve("DIM-1"));
        delete(level.resolve("DIM1"));
        delete(level.resolve("advancements"));
        delete(level.resolve("data"));
        delete(level.resolve("entities"));
        delete(level.resolve("playerdata"));
        delete(level.resolve("poi"));
        delete(level.resolve("region"));
        delete(level.resolve("stats"));
    }

    private void delete(Path path) {
        try {
            if (!Files.isDirectory(path)) Files.deleteIfExists(path);
            else try (var files = Files.list(path)) {
                files.forEach(this::delete);
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            plugin.getComponentLogger().warn("Failed to delete {}", path, e);
        }
    }
}
