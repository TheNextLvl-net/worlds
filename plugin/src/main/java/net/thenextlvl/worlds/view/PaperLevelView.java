package net.thenextlvl.worlds.view;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.thenextlvl.nbt.NBTInputStream;
import net.thenextlvl.nbt.NBTOutputStream;
import net.thenextlvl.worlds.Level;
import net.thenextlvl.worlds.WorldOperationException;
import net.thenextlvl.worlds.WorldRegistry;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.event.WorldCloneEvent;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NullMarked
public class PaperLevelView {
    private static final Key OVERWORLD = Key.key("overworld");
    private static final Key NETHER = Key.key("the_nether");
    private static final Key END = Key.key("the_end");

    private static final Set<String> SKIP_FILES = Set.of("metadata.dat");

    protected final WorldsPlugin plugin;

    public PaperLevelView(final WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    public World getOverworld() {
        return Optional.ofNullable(plugin.getServer().getWorld(OVERWORLD))
                .orElseThrow(() -> new IllegalStateException("Overworld not found"));
    }

    public boolean isOverworld(final World world) {
        return world.key().equals(OVERWORLD);
    }

    public boolean isNether(final World world) {
        return world.key().equals(NETHER);
    }

    public boolean isEnd(final World world) {
        return world.key().equals(END);
    }

    public Optional<World> getTarget(final World world, final PortalType type) {
        return switch (type) {
            case NETHER -> switch (world.getEnvironment()) {
                case NORMAL, CUSTOM, THE_END -> findRelatedWorld(world, World.Environment.NETHER);
                case NETHER -> findPrimaryWorld(world);
            };
            case ENDER -> switch (world.getEnvironment()) {
                case NORMAL, CUSTOM, NETHER -> findRelatedWorld(world, World.Environment.THE_END);
                case THE_END -> findPrimaryWorld(world);
            };
            default -> Optional.empty();
        };
    }

    private Optional<World> findRelatedWorld(final World source, final World.Environment environment) {
        return plugin.getServer().getWorlds().stream()
                .filter(world -> !world.equals(source))
                .filter(world -> sameDimensionsGroup(source, world))
                .filter(world -> world.getEnvironment().equals(environment))
                .findFirst();
    }

    private Optional<World> findPrimaryWorld(final World source) {
        return plugin.getServer().getWorlds().stream()
                .filter(world -> !world.equals(source))
                .filter(world -> sameDimensionsGroup(source, world))
                .filter(world -> world.getEnvironment().equals(World.Environment.NORMAL)
                        || world.getEnvironment().equals(World.Environment.CUSTOM))
                .findFirst();
    }

    private boolean sameDimensionsGroup(final World first, final World second) {
        return first.key().namespace().equals(second.key().namespace());
    }

    public Level.Builder read(final Key key, final WorldRegistry.Entry entry) {
        return Level.builder(key)
                .dimension(entry.dimension())
                .generator(entry.generator());
    }

    public Optional<Key> key(final Path directory) {
        return key(directory, false);
    }

    public Optional<Key> lenientKey(final Path directory) {
        return key(directory, true);
    }

    private Optional<Key> key(final Path directory, final boolean lenient) {
        final var dimensions = plugin.getDimensionsRoot().toAbsolutePath().normalize();
        final var absolute = directory.toAbsolutePath().normalize();
        final var relative = relativeLevelPath(absolute, dimensions, directory);
        return parseKey(relative, lenient);
    }

    private Path relativeLevelPath(final Path absolute, final Path dimensions, final Path fallback) {
        if (absolute.startsWith(dimensions)) return dimensions.relativize(absolute);

        final var container = plugin.getServer().getWorldContainer().toPath().toAbsolutePath().normalize();
        return absolute.startsWith(container) ? container.relativize(absolute) : fallback;
    }

    @SuppressWarnings("PatternValidation")
    static Optional<Key> parseKey(final Path relative, final boolean lenient) {
        if (relative.getNameCount() < 2) return Optional.empty();

        final var namespace = keySegment(relative, 0, lenient);
        if (!namespace.matches("[a-z0-9_\\-.]+")) return Optional.empty();

        final var value = keyValue(relative, lenient);
        if (!value.matches("[a-z0-9_\\-./]+")) return Optional.empty();

        return Optional.of(Key.key(namespace, value));
    }

    private static String keyValue(final Path path, final boolean lenient) {
        final var joiner = new StringJoiner("/");
        for (var index = 1; index < path.getNameCount(); index++) {
            joiner.add(keySegment(path, index, lenient));
        }
        return joiner.toString();
    }

    private static String keySegment(final Path path, final int index, final boolean lenient) {
        final var segment = path.getName(index).toString();
        return lenient ? createKey(segment) : segment;
    }

    public Stream<Path> listLevels() {
        return plugin.getWorldRegistry().worlds()
                .map(plugin::resolveLevelDirectory);
    }

    public Stream<Path> listLevelFolders() {
        return listDirectories().stream()
                .filter(path -> Files.isDirectory(path.resolve("region")));
    }

    private @Unmodifiable Set<Path> listDirectories() {
        if (!Files.isDirectory(plugin.getDimensionsRoot())) return Set.of();
        try (final var namespaces = Files.list(plugin.getDimensionsRoot())) {
            return namespaces.filter(Files::isDirectory).<Path>mapMulti((path, consumer) -> {
                try (final var files = Files.list(path)) {
                    files.filter(Files::isDirectory).forEach(consumer);
                } catch (final IOException ignored) {
                }
            }).collect(Collectors.toUnmodifiableSet());
        } catch (final IOException e) {
            return Set.of();
        }
    }

    public CompletableFuture<Boolean> unloadAsync(final World world, final boolean save) {
        return saveLevelDataAsync(world).thenCompose(ignored -> {
            plugin.getServer().allowPausing(plugin, false);
            return plugin.supplyGlobal(() -> {
                final var dragonBattle = world.getEnderDragonBattle();
                if (!plugin.getServer().unloadWorld(world, save))
                    return CompletableFuture.completedFuture(false);
                if (dragonBattle != null) dragonBattle.getBossBar().removeAll();
                plugin.getServer().allowPausing(plugin, true);
                return CompletableFuture.completedFuture(true);
            });
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to save level data before unloading", throwable);
            return false;
        });
    }

    public CompletableFuture<@Nullable Void> saveAsync(final World world, final boolean flush) {
        return plugin.supplyGlobal(() -> {
            try {
                return plugin.handler().saveAsync(world, flush);
            } catch (final Exception e) {
                WorldsPlugin.ERROR_TRACKER.trackError(e);
                return CompletableFuture.failedFuture(e);
            }
        }).thenRun(() -> saveLevelDataAsync(world));
    }

    public CompletableFuture<@Nullable Void> saveLevelDataAsync(final World world) {
        return plugin.handler().saveLevelDataAsync(world);
    }

    @SuppressWarnings("PatternValidation")
    public Key findFreeKey(final Key key) {
        return findFreeKey(key.namespace(), key.value());
    }

    @SuppressWarnings("PatternValidation")
    public Key findFreeKey(@KeyPattern.Namespace final String namespace, @KeyPattern.Value final String value) {
        final var usedValues = plugin.getServer().getWorlds().stream()
                .map(World::key)
                .filter(key -> key.namespace().equals(namespace))
                .map(Key::value)
                .collect(Collectors.toSet());
        return Key.key(namespace, findFreeValue(usedValues, value));
    }

    public static String findFreeValue(final Set<String> usedValues, final String value) {
        if (!usedValues.contains(value)) return value;

        var baseValue = value;
        int suffix = 1;
        String candidate = baseValue + "_1";

        final var pattern = Pattern.compile("^(.+)_(\\d+)$");
        final var matcher = pattern.matcher(value);

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

    public static @Subst("pattern") String createKey(final String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9_\\-./ ]+", "")
                .replace(" ", "_");
    }

    public void regenerate(final Path level, final long seed) {
        final var data = level.resolve("data");
        final var minecraft = data.resolve("minecraft");
        delete(minecraft.resolve("raids.dat"));
        replaceSeed(minecraft.resolve("world_gen_settings.dat"), seed);
        delete(level.resolve("entities"));
        delete(level.resolve("poi"));
        delete(level.resolve("region"));
    }

    public void delete(final Path level, final Key key) {
        if (key.equals(OVERWORLD)) {
            deleteOverworld(level);
        } else {
            delete(level, protectedLevels(level, key));
            deleteEmptyParents(level);
        }
    }

    private Set<Path> protectedLevels(final Path level, final Key key) {
        final var normalized = level.toAbsolutePath().normalize();
        final var registered = plugin.getWorldRegistry().worlds()
                .filter(world -> !world.equals(key))
                .map(plugin::resolveLevelDirectory);
        final var loaded = plugin.getServer().getWorlds().stream()
                .filter(world -> !world.key().equals(key))
                .map(World::getWorldPath);
        return Stream.concat(registered, loaded)
                .map(path -> path.toAbsolutePath().normalize())
                .filter(path -> path.startsWith(normalized))
                .collect(Collectors.toUnmodifiableSet());
    }

    private void deleteEmptyParents(final Path level) {
        final var root = plugin.getDimensionsRoot().toAbsolutePath().normalize();
        deleteEmptyParents(level.toAbsolutePath().normalize(), root);
    }

    private void deleteEmptyParents(final Path path, final Path root) {
        final var parent = path.getParent();
        if (parent == null || !parent.startsWith(root)) return;
        if (deleteIfEmpty(parent)) deleteEmptyParents(parent, root);
    }

    private boolean deleteIfEmpty(final Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (final DirectoryNotEmptyException ignored) {
            return false;
        } catch (final IOException e) {
            plugin.getComponentLogger().warn("Failed to delete {}", path, e);
            return false;
        }
    }

    private void deleteOverworld(final Path level) {
        final var data = level.resolve("data");
        final var minecraft = data.resolve("minecraft");
        final var paper = data.resolve("paper");

        delete(level, Set.of(
                minecraft.resolve("world_gen_settings.dat"),
                paper.resolve("metadata.dat"),
                paper.resolve("level_overrides.dat")
        ).stream().map(path -> path.toAbsolutePath().normalize()).collect(Collectors.toUnmodifiableSet()));
    }

    public void delete(final Path path) {
        delete(path, Set.of());
    }

    private void delete(final Path path, final Set<Path> skipped) {
        try {
            if (skipped.contains(path.toAbsolutePath().normalize())) return;
            if (!Files.isDirectory(path)) Files.deleteIfExists(path);
            else try (final var files = Files.list(path)) {
                files.forEach(file -> delete(file, skipped));
                try (final var remaining = Files.list(path)) {
                    if (remaining.findAny().isEmpty()) Files.deleteIfExists(path);
                }
            }
        } catch (final IOException e) {
            plugin.getComponentLogger().warn("Failed to delete {}", path, e);
        }
    }

    private void replaceSeed(final Path path, final long seed) {
        if (!Files.isRegularFile(path)) return;
        try (final var input = NBTInputStream.create(path)) {
            final var root = input.readTag();
            final var data = root.getAsCompound("data").toBuilder()
                    .put("seed", seed)
                    .build();
            final var updated = root.toBuilder().put("data", data).build();
            try (final var output = NBTOutputStream.create(path)) {
                output.writeTag(null, updated);
            }
        } catch (final IOException | RuntimeException e) {
            plugin.getComponentLogger().warn("Failed to replace seed in {}", path, e);
        }
    }

    public CompletableFuture<World> cloneAsync(final World world, final Consumer<Level.Builder> builder, final boolean full) {
        return plugin.supplyGlobal(() -> cloneInternal(world, builder, full));
    }

    private CompletableFuture<World> cloneInternal(final World world, final Consumer<Level.Builder> builder, final boolean full) {
        final var levelBuilder = Level.copy(world);

        levelBuilder.key(findFreeKey(world.key()));

        builder.accept(levelBuilder);
        final var clone = levelBuilder.build();

        try {
            if (plugin.getServer().getWorld(clone.key()) != null) throw new WorldOperationException(
                    WorldOperationException.Reason.WORLD_KEY_EXISTS
            ).key(clone.key());
            if (plugin.getServer().getWorld(clone.getName()) != null) throw new WorldOperationException(
                    WorldOperationException.Reason.WORLD_NAME_EXISTS
            ).key(clone.getName());
            if (Files.exists(clone.getDirectory())) throw new WorldOperationException(
                    Files.isDirectory(clone.getDirectory())
                            ? WorldOperationException.Reason.WORLD_PATH_EXISTS
                            : WorldOperationException.Reason.TARGET_PATH_IS_FILE
            );
        } catch (final RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }

        final var event = new WorldCloneEvent(world, clone, full);
        event.callEvent();

        return full ? saveAsync(world, true).thenCompose(ignored -> {
            try {
                copyDirectory(world.getWorldPath(), clone.getDirectory(), event.getFileFilter());
                return clone.create();
            } catch (final IOException e) {
                return CompletableFuture.failedFuture(new WorldOperationException(
                        WorldOperationException.Reason.BACKUP_WRITE_FAILED, e
                ).key(clone.key()).path(clone.getDirectory()));
            }
        }) : clone.create();
    }

    public void copyDirectory(final Path source, final Path destination, @Nullable final BiPredicate<Path, BasicFileAttributes> filter) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path path, final BasicFileAttributes attributes) throws IOException {
                if (filter != null && !filter.test(path, attributes)) return FileVisitResult.SKIP_SUBTREE;
                Files.createDirectories(destination.resolve(source.relativize(path)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes) throws IOException {
                if (SKIP_FILES.contains(path.getFileName().toString())) return FileVisitResult.CONTINUE;
                if (filter != null && !filter.test(path, attributes)) return FileVisitResult.CONTINUE;
                Files.copy(path, destination.resolve(source.relativize(path)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path path, final IOException exc) {
                plugin.getComponentLogger().warn("Failed to copy file: {}", path, exc);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
