package net.thenextlvl.worlds.view;

import com.google.common.base.Preconditions;
import core.io.IO;
import core.nbt.file.NBTFile;
import core.nbt.tag.CompoundTag;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.event.WorldActionScheduledEvent;
import net.thenextlvl.worlds.api.event.WorldActionScheduledEvent.ActionType;
import net.thenextlvl.worlds.api.event.WorldBackupEvent;
import net.thenextlvl.worlds.api.event.WorldCloneEvent;
import net.thenextlvl.worlds.api.event.WorldDeleteEvent;
import net.thenextlvl.worlds.api.event.WorldRegenerateEvent;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.view.LevelView;
import net.thenextlvl.worlds.level.LevelData;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import static org.bukkit.persistence.PersistentDataType.BOOLEAN;
import static org.bukkit.persistence.PersistentDataType.STRING;

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

    /**
     * @see net.minecraft.world.level.storage.LevelStorageSource#createDefault(Path)
     */
    @Override
    public Path getBackupFolder() {
        return getWorldContainer().resolve("../backups");
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
            return stream.collect(Collectors.toUnmodifiableSet());
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
     * @see net.minecraft.server.level.ServerLevel#saveIncrementally(boolean)
     * @see net.minecraft.server.level.ServerLevel#saveLevelData(boolean)
     */
    @Override
    public void saveLevelData(World world, boolean async) {
        var level = ((CraftWorld) world).getHandle();
        if (level.getDragonFight() != null) {
            level.serverLevelData.setEndDragonFightData(level.getDragonFight().saveData());
        }

        level.serverLevelData.setWorldBorder(level.getWorldBorder().createSettings());
        level.serverLevelData.setCustomBossEvents(level.getServer().getCustomBossEvents().save(level.registryAccess()));

        var save = level.getChunkSource().getDataStorage().scheduleSave();
        if (!async) save.join();
    }

    @Deprecated(forRemoval = true)
    public void persistWorld(World world, boolean enabled) {
        var worldKey = new NamespacedKey("worlds", "world_key");
        world.getPersistentDataContainer().set(worldKey, STRING, world.getKey().asString());
        persistStatus(world, enabled, true);
    }

    @Deprecated(forRemoval = true)
    public void persistStatus(World world, boolean enabled, boolean force) {
        var enabledKey = new NamespacedKey("worlds", "enabled");
        if (!force && !world.getPersistentDataContainer().has(enabledKey)) return;
        world.getPersistentDataContainer().set(enabledKey, BOOLEAN, enabled);
    }

    @Deprecated(forRemoval = true)
    public void persistGenerator(World world, Generator generator) {
        var generatorKey = new NamespacedKey("worlds", "generator");
        world.getPersistentDataContainer().set(generatorKey, STRING, generator.asString());
    }

    @Override
    public long backup(World world) throws IOException {
        new WorldBackupEvent(world).callEvent();
        save(world, true);
        return ((CraftWorld) world).getHandle().levelStorageAccess.makeWorldBackup();
    }

    private String findFreeName(String name) {
        var usedNames = plugin.getServer().getWorlds().stream()
                .map(WorldInfo::getName)
                .collect(Collectors.toSet());
        return findFreeName(usedNames, name);
    }

    private Path findFreePath(String name) {
        var usedPaths = listDirectories().stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toSet());
        return Path.of(findFreeName(usedPaths, name));
    }

    public static String findFreeName(Set<String> usedNames, String name) {
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
    public Optional<World> clone(World world, Consumer<Level.Builder> builder, boolean full) throws IllegalArgumentException, IllegalStateException, IOException {
        var levelBuilder = plugin.levelBuilder(world);

        var name = findFreeName(world.getName());
        levelBuilder.name(name);
        levelBuilder.key(Key.key(world.key().namespace(), LevelData.createKey(name)));
        levelBuilder.directory(findFreePath(world.getWorldFolder().getName()));

        builder.accept(levelBuilder);
        var clone = levelBuilder.build();

        Preconditions.checkArgument(plugin.getServer().getWorld(clone.key()) == null, "World with key %s already exists", clone.key());
        Preconditions.checkArgument(plugin.getServer().getWorld(clone.getName()) == null, "World with name %s already exists", clone.getName());
        Preconditions.checkState(!Files.isDirectory(clone.getDirectory()), "Target directory already exists");

        var event = new WorldCloneEvent(world, clone, full);
        event.callEvent();

        if (full) {
            save(world, true);
            copyDirectory(world.getWorldFolder().toPath(), clone.getDirectory(), event.getFileFilter());
        }

        return clone.create();
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

    private void copyDirectory(Path source, Path destination, @Nullable BiPredicate<Path, BasicFileAttributes> filter) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes) throws IOException {
                if (switch (path.getFileName().toString()) {
                    case "advancements", "datapacks", "playerdata", "stats" -> true;
                    default -> false;
                }) return FileVisitResult.SKIP_SUBTREE;
                if (filter != null && !filter.test(path, attributes)) return FileVisitResult.SKIP_SUBTREE;
                Files.createDirectories(destination.resolve(source.relativize(path)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
                if (switch (path.getFileName().toString()) {
                    case "uid.dat", "session.lock" -> true;
                    default -> false;
                }) return FileVisitResult.CONTINUE;
                if (filter != null && !filter.test(path, attributes)) return FileVisitResult.CONTINUE;
                Files.copy(path, destination.resolve(source.relativize(path)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException exc) {
                plugin.getComponentLogger().error("Failed to copy file: {}", path, exc);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
