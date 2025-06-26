package net.thenextlvl.worlds.view;

import com.google.common.base.Preconditions;
import core.io.IO;
import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import net.kyori.adventure.key.Key;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.event.WorldActionScheduledEvent;
import net.thenextlvl.worlds.api.event.WorldActionScheduledEvent.ActionType;
import net.thenextlvl.worlds.api.event.WorldBackupEvent;
import net.thenextlvl.worlds.api.event.WorldCloneEvent;
import net.thenextlvl.worlds.api.event.WorldDeleteEvent;
import net.thenextlvl.worlds.api.event.WorldRegenerateEvent;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.view.LevelView;
import net.thenextlvl.worlds.level.LevelData;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spigotmc.AsyncCatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import static org.bukkit.persistence.PersistentDataType.BOOLEAN;

@NullMarked
public class PaperLevelView implements LevelView {
    private static final NamespacedKey ENABLED_KEY = new NamespacedKey("worlds", "enabled");

    private static final Set<String> SKIP_DIRECTORIES = Set.of("advancements", "datapacks", "playerdata", "stats");
    private static final Set<String> SKIP_FILES = Set.of("uid.dat", "session.lock");

    private final Map<Key, Runnable> deletions = new ConcurrentHashMap<>();
    private final Map<Key, Runnable> regenerations = new ConcurrentHashMap<>();
    protected final WorldsPlugin plugin;

    public PaperLevelView(WorldsPlugin plugin) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            deletions.values().forEach(Runnable::run);
            regenerations.values().forEach(Runnable::run);
        }, "Worlds Shutdown Hook"));
        this.plugin = plugin;
    }

    @SuppressWarnings("resource")
    public World getOverworld() {
        var handle = ((CraftServer) plugin.getServer()).getHandle();
        return handle.getServer().overworld().getWorld();
    }

    public boolean isOverworld(World world) {
        return world.equals(getOverworld());
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

    /**
     * @see LevelStorageSource#createDefault(Path)
     */
    @Override
    public Path getBackupFolder() {
        return getWorldContainer().getParent().resolve("backups");
    }

    @Override
    public Path getWorldContainer() {
        return plugin.getServer().getWorldContainer().toPath();
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
    public @Unmodifiable Set<Path> listLevels() {
        return listDirectories().stream()
                .filter(this::isLevel)
                .collect(Collectors.toUnmodifiableSet());
    }

    private @Unmodifiable Set<Path> listDirectories() {
        try (var stream = Files.list(plugin.getServer().getWorldContainer().toPath())) {
            return stream.filter(Files::isDirectory).collect(Collectors.toUnmodifiableSet());
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
    public CompletableFuture<Boolean> unloadAsync(World world, boolean save) {
        return saveLevelDataAsync(world).thenCompose(ignored -> {
            var future = new CompletableFuture<Boolean>();
            plugin.getServer().getGlobalRegionScheduler().run(plugin, scheduledTask -> {
                var dragonBattle = world.getEnderDragonBattle();
                if (plugin.getServer().unloadWorld(world, save)) {
                    if (dragonBattle != null) dragonBattle.getBossBar().removeAll();
                    future.complete(true);
                } else future.complete(false);
            });
            return future;
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to save level data before unloading", throwable);
            return false;
        });
    }

    /**
     * @see CraftWorld#save(boolean)
     */
    @Override
    public CompletableFuture<@Nullable Void> saveAsync(World world, boolean flush) {
        var future = new CompletableFuture<@Nullable Void>();
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            try {
                var level = ((CraftWorld) world).getHandle();
                var oldSave = level.noSave;
                level.noSave = false;
                level.save(null, flush, false);
                level.noSave = oldSave;
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future.thenRunAsync(() -> saveLevelDataAsync(world));
    }

    /**
     * @see ServerLevel#saveIncrementally(boolean)
     * @see ServerLevel#saveLevelData(boolean)
     */
    @Override
    public CompletableFuture<@Nullable Void> saveLevelDataAsync(World world) {
        var level = ((CraftWorld) world).getHandle();
        if (level.getDragonFight() != null) {
            level.serverLevelData.setEndDragonFightData(level.getDragonFight().saveData());
        }

        level.serverLevelData.setWorldBorder(level.getWorldBorder().createSettings());
        level.serverLevelData.setCustomBossEvents(level.getServer().getCustomBossEvents().save(level.registryAccess()));
        level.levelStorageAccess.saveDataTag(level.getServer().registryAccess(), level.serverLevelData, null);

        return level.getChunkSource().getDataStorage().scheduleSave().thenApply(ignored -> null);
    }

    @Override
    public boolean isEnabled(World world) {
        return Boolean.TRUE.equals(world.getPersistentDataContainer().get(ENABLED_KEY, BOOLEAN));
    }

    @Override
    public void setEnabled(World world, boolean enabled) {
        world.getPersistentDataContainer().set(ENABLED_KEY, BOOLEAN, enabled);
    }

    @Override
    public CompletableFuture<Long> backupAsync(World world) {
        AsyncCatcher.catchOp("world backup");
        new WorldBackupEvent(world).callEvent();
        return saveAsync(world, true).thenComposeAsync(ignored -> {
            try {
                var size = ((CraftWorld) world).getHandle().levelStorageAccess.makeWorldBackup();
                return CompletableFuture.completedFuture(size);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    public String findFreeName(String name) {
        var usedNames = plugin.getServer().getWorlds().stream()
                .map(WorldInfo::getName)
                .collect(Collectors.toSet());
        return findFreeName(usedNames, name);
    }

    public Path findFreePath(String name) {
        var usedPaths = listDirectories().stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toSet());
        return Path.of(findFreeName(usedPaths, name));
    }

    public static String findFreeName(Set<String> usedNames, String name) {
        if (!usedNames.contains(name)) return name;

        var baseName = name;
        int suffix = 1;
        String candidate = baseName + " (1)";

        var pattern = Pattern.compile("^(.+) \\((\\d+)\\)$");
        var matcher = pattern.matcher(name);

        if (matcher.matches()) {
            baseName = matcher.group(1);
            suffix = Integer.parseInt(matcher.group(2)) + 1;
            candidate = baseName + " (" + suffix + ")";
            suffix++;
        }

        while (usedNames.contains(candidate)) {
            candidate = baseName + " (" + suffix++ + ")";
        }

        return candidate;
    }


    @Override
    @SuppressWarnings("PatternValidation")
    public CompletableFuture<World> cloneAsync(World world, Consumer<Level.Builder> builder, boolean full) {
        AsyncCatcher.catchOp("world cloning");
        var levelBuilder = plugin.levelBuilder(world);

        var name = findFreeName(world.getName());
        levelBuilder.name(name);
        levelBuilder.key(Key.key(world.key().namespace(), LevelData.createKey(name)));
        levelBuilder.directory(findFreePath(world.getWorldFolder().getName()));

        builder.accept(levelBuilder);
        var clone = levelBuilder.build();

        try {
            Preconditions.checkArgument(plugin.getServer().getWorld(clone.key()) == null, "World with key %s already exists", clone.key());
            Preconditions.checkArgument(plugin.getServer().getWorld(clone.getName()) == null, "World with name %s already exists", clone.getName());
            Preconditions.checkState(!Files.isDirectory(clone.getDirectory()), "Target directory already exists");
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }

        var event = new WorldCloneEvent(world, clone, full);
        event.callEvent();

        return full ? saveAsync(world, true).thenCompose(ignored -> {
            try {
                copyDirectory(world.getWorldFolder().toPath(), clone.getDirectory(), event.getFileFilter());
                return clone.createAsync();
            } catch (IOException e) {
                return CompletableFuture.failedFuture(e);
            }
        }) : clone.createAsync();
    }

    @Override
    public CompletableFuture<DeletionResult> deleteAsync(World world, boolean schedule) {
        AsyncCatcher.catchOp("world deletion");
        return schedule ? CompletableFuture.completedFuture(scheduleDeletion(world)) : deleteNow(world);
    }

    @Override
    public boolean cancelScheduledDeletion(World world) {
        return deletions.remove(world.key()) != null;
    }

    @Override
    public boolean isDeletionScheduled(World world) {
        return deletions.containsKey(world.key());
    }

    @Override
    public CompletableFuture<DeletionResult> regenerateAsync(World world, boolean schedule) {
        AsyncCatcher.catchOp("world regeneration");
        return schedule ? CompletableFuture.completedFuture(scheduleRegeneration(world)) : regenerateNow(world);
    }

    @Override
    public boolean cancelScheduledRegeneration(World world) {
        return regenerations.remove(world.key()) != null;
    }

    @Override
    public boolean isRegenerationScheduled(World world) {
        return regenerations.containsKey(world.key());
    }

    private CompletableFuture<DeletionResult> deleteNow(World world) {
        if (isOverworld(world)) return CompletableFuture.completedFuture(DeletionResult.REQUIRES_SCHEDULING);

        if (!new WorldDeleteEvent(world).callEvent())
            return CompletableFuture.completedFuture(DeletionResult.FAILED);

        var fallback = getOverworld().getSpawnLocation();
        return CompletableFuture.allOf(world.getPlayers().stream()
                .map(player -> player.teleportAsync(fallback))
                .toList().toArray(new CompletableFuture[0])
        ).thenCompose(ignored -> unloadAsync(world, false).thenApplyAsync(success -> {
            if (!success) return DeletionResult.UNLOAD_FAILED;
            delete(world.getWorldFolder().toPath());
            deletions.remove(world.key());
            return DeletionResult.SUCCESS;
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to delete world", throwable);
            return DeletionResult.FAILED;
        }));
    }

    private DeletionResult scheduleDeletion(World world) {
        return scheduleAction(world, ActionType.DELETE, deletions, this::delete);
    }

    private DeletionResult scheduleAction(World world, ActionType type, Map<Key, Runnable> map, Consumer<Path> consumer) {
        if (map.containsKey(world.key())) return DeletionResult.SCHEDULED;

        var event = new WorldActionScheduledEvent(world, type);
        if (!event.callEvent()) return DeletionResult.FAILED;

        var action = event.getAction() == null ? consumer : event.getAction().andThen(consumer);

        var path = world.getWorldFolder().toPath();
        map.put(world.key(), () -> action.accept(path));
        return DeletionResult.SCHEDULED;
    }

    private CompletableFuture<DeletionResult> regenerateNow(World world) {
        if (isOverworld(world)) return CompletableFuture.completedFuture(DeletionResult.REQUIRES_SCHEDULING);

        if (!new WorldRegenerateEvent(world).callEvent())
            return CompletableFuture.completedFuture(DeletionResult.FAILED);

        var players = world.getPlayers();
        var fallback = getOverworld().getSpawnLocation();
        return CompletableFuture.allOf(players.stream()
                .map(player -> player.teleportAsync(fallback, TeleportCause.PLUGIN))
                .toList().toArray(new CompletableFuture[0])
        ).thenCompose(ignored -> saveLevelDataAsync(world).thenCompose(ignored1 -> {
            return unloadAsync(world, false).thenCompose(success -> {
                if (!success) return CompletableFuture.completedFuture(DeletionResult.UNLOAD_FAILED);

                regenerate(world.getWorldFolder().toPath());
                regenerations.remove(world.key());
                return plugin.levelBuilder(world).build().createAsync().thenAccept(regenerated -> {
                    players.forEach(player -> player.teleportAsync(regenerated.getSpawnLocation(), TeleportCause.PLUGIN));
                }).thenApply(ignored2 -> DeletionResult.SUCCESS);
            }).exceptionally(throwable -> {
                plugin.getComponentLogger().warn("Failed to regenerate world", throwable);
                return DeletionResult.FAILED;
            });
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to save level data before regeneration", throwable);
            return DeletionResult.FAILED;
        }));
    }

    private DeletionResult scheduleRegeneration(World world) {
        return scheduleAction(world, ActionType.REGENERATE, regenerations, this::regenerate);
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

    private void copyDirectory(Path source, Path destination, @Nullable BiPredicate<Path, BasicFileAttributes> filter) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes) throws IOException {
                if (SKIP_DIRECTORIES.contains(path.getFileName().toString())) return FileVisitResult.SKIP_SUBTREE;
                if (filter != null && !filter.test(path, attributes)) return FileVisitResult.SKIP_SUBTREE;
                Files.createDirectories(destination.resolve(source.relativize(path)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
                if (SKIP_FILES.contains(path.getFileName().toString())) return FileVisitResult.CONTINUE;
                if (filter != null && !filter.test(path, attributes)) return FileVisitResult.CONTINUE;
                Files.copy(path, destination.resolve(source.relativize(path)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException exc) {
                plugin.getComponentLogger().warn("Failed to copy file: {}", path, exc);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
