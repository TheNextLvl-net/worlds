package net.thenextlvl.worlds.view;

import com.google.common.base.Preconditions;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minecraft.FileUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.thenextlvl.nbt.NBTInputStream;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.event.WorldActionScheduledEvent;
import net.thenextlvl.worlds.api.event.WorldActionScheduledEvent.ActionType;
import net.thenextlvl.worlds.api.event.WorldBackupEvent;
import net.thenextlvl.worlds.api.event.WorldBackupRestoreEvent;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardOpenOption.READ;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import static org.bukkit.persistence.PersistentDataType.BOOLEAN;

@NullMarked
public class PaperLevelView implements LevelView {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
            .withZone(ZoneId.systemDefault());
    private static final NamespacedKey ENABLED_KEY = new NamespacedKey("worlds", "enabled");

    private static final Set<String> SKIP_DIRECTORIES = Set.of("advancements", "datapacks", "playerdata", "stats");
    private static final Set<String> SKIP_FILES = Set.of("uid.dat", "session.lock");

    private final Map<Key, Runnable> backupRestorations = new ConcurrentHashMap<>();
    private final Map<Key, Runnable> deletions = new ConcurrentHashMap<>();
    private final Map<Key, Runnable> regenerations = new ConcurrentHashMap<>();
    protected final WorldsPlugin plugin;

    public PaperLevelView(WorldsPlugin plugin) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            backupRestorations.values().forEach(Runnable::run);
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

    public Optional<CompoundTag> getLevelDataFile(Path level) {
        return getLevelDataPath(level).map(path -> {
            try (var inputStream = new NBTInputStream(
                    Files.newInputStream(path, READ),
                    StandardCharsets.UTF_8
            )) {
                return inputStream.readTag().getAsCompound();
            } catch (IOException e) {
                plugin.getComponentLogger().warn("Failed to read level data from {}", path, e);
                return null;
            }
        });
    }

    private static @Nullable Path getFile(Path level, String other) {
        var resolved = level.resolve(other);
        return Files.isRegularFile(resolved) ? resolved : null;
    }

    /**
     * @see LevelStorageSource#createDefault(Path)
     */
    @Override
    public Path getBackupFolder() {
        var backupFolder = System.getenv("WORLDS_BACKUP_FOLDER");
        if (backupFolder == null) backupFolder = System.getProperty("worlds.backup.folder");
        if (backupFolder != null) return Path.of(backupFolder);
        var parent = getWorldContainer().getParent();
        return parent != null ? parent.resolve("backups") : Path.of("backups");
    }

    @Override
    public Path getBackupFolder(World world) {
        return getBackupFolder().resolve(world.getName());
    }

    @Override
    public Path getWorldContainer() {
        return plugin.getServer().getWorldContainer().toPath();
    }

    @Override
    public String getEntryPermission(World world) {
        return "worlds.enter." + world.key().asString();
    }

    @Override
    public Optional<Level.Builder> read(Path directory) {
        try {
            return LevelData.read(plugin, directory);
        } catch (Exception e) {
            if (e.getCause() instanceof ZipException) {
                plugin.getComponentLogger().warn("Failed to read level data from {}", directory);
                plugin.getComponentLogger().warn("Your level.dat is irrecoverably corrupted. Please delete it and recreate the world.");
            } else plugin.getComponentLogger().warn("Failed to read level data from {}", directory, e);
            return Optional.empty();
        }
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
            return plugin.supplyGlobal(() -> {
                var dragonBattle = world.getEnderDragonBattle();
                if (!plugin.getServer().unloadWorld(world, save))
                    return CompletableFuture.completedFuture(false);
                if (dragonBattle != null) dragonBattle.getBossBar().removeAll();
                return CompletableFuture.completedFuture(true);
            });
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
        return plugin.supplyGlobal(() -> {
            try {
                var level = ((CraftWorld) world).getHandle();
                var oldSave = level.noSave;
                level.noSave = false;
                level.save(null, flush, false);
                level.noSave = oldSave;
                return CompletableFuture.completedFuture(null);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        }).thenRun(() -> saveLevelDataAsync(world));
    }

    /**
     * @see ServerLevel#saveIncrementally(boolean)
     * @see ServerLevel#saveLevelData(boolean)
     */
    @Override
    @SuppressWarnings("JavadocReference")
    public CompletableFuture<@Nullable Void> saveLevelDataAsync(World world) {
        var level = ((CraftWorld) world).getHandle();
        if (level.getDragonFight() != null) {
            level.serverLevelData.setEndDragonFightData(level.getDragonFight().saveData());
        }

        level.serverLevelData.setCustomBossEvents(level.getServer().getCustomBossEvents().save(level.registryAccess()));
        level.levelStorageAccess.saveDataTag(level.getServer().registryAccess(), level.serverLevelData, level.getServer().getPlayerList().getSingleplayerData());

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
    public CompletableFuture<Path> createBackupAsync(World world, @Nullable String name) {
        return plugin.supplyGlobal(() -> {
            new WorldBackupEvent(world).callEvent();
            return saveAsync(world, true).thenComposeAsync(ignored -> {
                try {
                    return CompletableFuture.completedFuture(backupInternal(world, name));
                } catch (IOException e) {
                    return CompletableFuture.failedFuture(e);
                }
            });
        });
    }

    @Override
    public CompletableFuture<RestoringResult> restoreBackupAsync(World world, Path backupFile, boolean schedule) {
        if (!Files.isRegularFile(backupFile))
            return CompletableFuture.completedFuture(new RestoringResultImpl(null, DeletionResult.FAILED));
        return plugin.supplyGlobal(() -> {
            if (schedule) return CompletableFuture.completedFuture(scheduleRestoreBackup(world, backupFile));
            if (isOverworld(world)) return CompletableFuture.completedFuture(
                    new RestoringResultImpl(null, DeletionResult.REQUIRES_SCHEDULING)
            );
            if (!new WorldBackupRestoreEvent(world, backupFile).callEvent())
                return CompletableFuture.completedFuture(new RestoringResultImpl(null, DeletionResult.FAILED));
            return restoreBackupInternal(world, backupFile);
        });
    }

    @Override
    public boolean cancelScheduledBackupRestoration(World world) {
        return backupRestorations.remove(world.key()) != null;
    }

    @Override
    public boolean isBackupRestorationScheduled(World world) {
        return backupRestorations.containsKey(world.key());
    }

    private RestoringResult scheduleRestoreBackup(World world, Path backupFile) {
        var deletionResult = scheduleAction(world, ActionType.RESTORE_BACKUP, backupRestorations, path -> {
            restore(path, backupFile);
        });
        return new RestoringResultImpl(null, deletionResult);
    }

    private record RestoringResultImpl(@Nullable World world, DeletionResult result) implements RestoringResult {
    }

    private CompletableFuture<RestoringResult> restoreBackupInternal(World world, Path path) {
        var players = world.getPlayers();
        var fallback = getOverworld().getSpawnLocation();
        return CompletableFuture.allOf(players.stream()
                .map(player -> player.teleportAsync(fallback, TeleportCause.PLUGIN).thenAccept(success -> {
                    if (!success) player.kick(plugin.bundle().component("world.unload.kicked", player));
                }))
                .toArray(CompletableFuture[]::new)
        ).thenCompose(ignored -> {
            var worldPath = world.getWorldFolder().toPath();
            return unloadAsync(world, true).thenCompose(success -> {
                if (!success) return CompletableFuture.<RestoringResult>completedFuture(
                        new RestoringResultImpl(null, DeletionResult.UNLOAD_FAILED)
                );
                restore(worldPath, path);
                backupRestorations.remove(world.key());
                return plugin.levelView().read(worldPath).orElseThrow().build().createAsync().thenApply(restored -> {
                    players.forEach(player -> player.teleportAsync(restored.getSpawnLocation(), TeleportCause.PLUGIN));
                    return new RestoringResultImpl(restored, DeletionResult.SUCCESS);
                });
            }).exceptionallyCompose(throwable -> {
                plugin.getComponentLogger().warn("Failed to restore backup", throwable);
                return plugin.levelBuilder(world).build().createAsync().thenApply(restored -> {
                    players.forEach(player -> player.teleportAsync(restored.getSpawnLocation(), TeleportCause.PLUGIN));
                    return new RestoringResultImpl(null, DeletionResult.FAILED);
                });
            });
        });
    }

    private void restore(Path worldPath, Path path) {
        Path tempPath;
        do {
            tempPath = worldPath.resolveSibling("." + UUID.randomUUID());
        } while (Files.isDirectory(tempPath));
        try (var input = new ZipInputStream(Files.newInputStream(path, StandardOpenOption.READ))) {
            ZipEntry entry;
            var root = tempPath.toAbsolutePath().normalize();
            while ((entry = input.getNextEntry()) != null) {
                Path resolved;
                try {
                    resolved = resolveZipEntry(root, entry);
                } catch (IOException e) {
                    plugin.getComponentLogger().warn("Skipping suspicious zip entry: {}", entry.getName(), e);
                    continue;
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(resolved);
                } else {
                    var parent = resolved.getParent();
                    if (parent != null) Files.createDirectories(parent);
                    Files.copy(input, resolved);
                }
            }
            Files.walkFileTree(worldPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, @Nullable IOException exc) throws IOException {
                    if (exc != null) throw exc;
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            Files.move(tempPath, worldPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ex) {
                plugin.getComponentLogger().warn("Failed to delete temporary files for backup restoration", ex);
            }
            throw new RuntimeException("Failed to restore backup from " + path + " to " + worldPath, e);
        }
    }

    private static Path resolveZipEntry(Path path, ZipEntry entry) throws IOException {
        var target = path.resolve(entry.getName()).normalize();
        if (!target.startsWith(path)) {
            throw new IOException("Zip entry outside target dir: " + entry.getName());
        }
        return target;
    }

    @Override
    public Stream<Path> listBackups(World world) {
        try {
            var files = Files.list(getBackupFolder(world));
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".zip"))
                    .onClose(files::close);
        } catch (IOException ignored) {
            return Stream.empty();
        }
    }

    /**
     * @see LevelStorageSource.LevelStorageAccess#makeWorldBackup()
     */
    private Path backupInternal(World world, @Nullable String name) throws IOException {
        var backupPath = getBackupFolder(world);
        Files.createDirectories(backupPath);
        var availableName = name != null ? name + ".zip" : FileUtil.findAvailableName(backupPath, FORMATTER.format(Instant.now()), ".zip");
        var path = backupPath.resolve(availableName);
        if (name != null && Files.isRegularFile(path)) {
            throw new FileAlreadyExistsException(path.toString(), null, "A Backup named " + name + " already exists for " + world.key());
        } else try (var output = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(
                path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE
        )))) {
            Files.walkFileTree(world.getWorldFolder().toPath(), new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.endsWith("session.lock")) {
                        var relative = world.getWorldFolder().toPath().relativize(file).toString().replace('\\', '/');
                        output.putNextEntry(new ZipEntry(relative));
                        Files.copy(file, output);
                        output.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return path;
    }

    public String findFreeName(String name) {
        var usedNames = plugin.getServer().getWorlds().stream()
                .map(WorldInfo::getName)
                .collect(Collectors.toSet());
        return findFreeName(usedNames, name);
    }

    @SuppressWarnings("PatternValidation")
    public Key findFreeKey(Key key) {
        return findFreeKey(key.namespace(), key.value());
    }

    @SuppressWarnings("PatternValidation")
    public Key findFreeKey(@KeyPattern.Namespace String namespace, @KeyPattern.Value String value) {
        var usedValues = plugin.getServer().getWorlds().stream()
                .map(World::key)
                .filter(key -> key.namespace().equals(namespace))
                .map(Key::value)
                .collect(Collectors.toSet());
        return Key.key(namespace, findFreeValue(usedValues, value));
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

    public static String findFreeValue(Set<String> usedValues, String value) {
        if (!usedValues.contains(value)) return value;

        var baseValue = value;
        int suffix = 1;
        String candidate = baseValue + "_1";

        var pattern = Pattern.compile("^(.+) \\((\\d+)\\)$");
        var matcher = pattern.matcher(value);

        if (matcher.matches()) {
            baseValue = matcher.group(1);
            suffix = Integer.parseInt(matcher.group(2)) + 1;
            candidate = baseValue + "_" + suffix;
            suffix++;
        }

        while (usedValues.contains(candidate)) {
            candidate = baseValue + "_" + suffix++;
        }

        return candidate;
    }

    @Override
    public CompletableFuture<World> cloneAsync(World world, Consumer<Level.Builder> builder, boolean full) {
        return plugin.supplyGlobal(() -> cloneInternal(world, builder, full));
    }

    private CompletableFuture<World> cloneInternal(World world, Consumer<Level.Builder> builder, boolean full) {
        var levelBuilder = plugin.levelBuilder(world);

        levelBuilder.name(findFreeName(world.getName()));
        levelBuilder.key(findFreeKey(world.key()));
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
        return plugin.supplyGlobal(() -> schedule ? CompletableFuture.completedFuture(scheduleDeletion(world)) : deleteNow(world));
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
    public CompletableFuture<DeletionResult> regenerateAsync(World world, boolean schedule, Consumer<Level.Builder> builder) {
        return plugin.supplyGlobal(() -> schedule ? CompletableFuture.completedFuture(scheduleRegeneration(world)) : regenerateNow(world, builder));
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
                .map(player -> player.teleportAsync(fallback, TeleportCause.PLUGIN).thenAccept(success -> {
                    if (!success) player.kick(plugin.bundle().component("world.unload.kicked", player));
                }))
                .toArray(CompletableFuture[]::new)
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

    private CompletableFuture<DeletionResult> regenerateNow(World world, Consumer<Level.Builder> consumer) {
        if (isOverworld(world)) return CompletableFuture.completedFuture(DeletionResult.REQUIRES_SCHEDULING);

        if (!new WorldRegenerateEvent(world).callEvent())
            return CompletableFuture.completedFuture(DeletionResult.FAILED);

        var players = world.getPlayers();
        var fallback = getOverworld().getSpawnLocation();
        return CompletableFuture.allOf(players.stream()
                .map(player -> player.teleportAsync(fallback, TeleportCause.PLUGIN).thenAccept(success -> {
                    if (!success) player.kick(plugin.bundle().component("world.unload.kicked", player));
                }))
                .toArray(CompletableFuture[]::new)
        ).thenCompose(ignored -> saveLevelDataAsync(world).thenCompose(ignored1 -> {
            return unloadAsync(world, false).thenCompose(success -> {
                if (!success) return CompletableFuture.completedFuture(DeletionResult.UNLOAD_FAILED);

                regenerate(world.getWorldFolder().toPath());
                regenerations.remove(world.key());
                var builder = plugin.levelBuilder(world);
                consumer.accept(builder);
                return builder.build().createAsync().thenAccept(regenerated -> {
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
