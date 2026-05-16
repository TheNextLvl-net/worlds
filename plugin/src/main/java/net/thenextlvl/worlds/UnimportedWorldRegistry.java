package net.thenextlvl.worlds;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@NullMarked
public abstract class UnimportedWorldRegistry<T> {
    private final Map<Path, T> worlds = new ConcurrentHashMap<>();
    protected final WorldsPlugin plugin;

    protected UnimportedWorldRegistry(final WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    public Stream<Path> listPaths(final Path path) throws IOException {
        return listEntries(path).map(Map.Entry::getKey);
    }

    public Stream<Map.Entry<Path, T>> listEntries(final Path path) throws IOException {
        return listCandidates(path)
                .map(path1 -> read(path1).map(data -> Map.entry(path1, data)).orElse(null))
                .filter(Objects::nonNull);
    }

    public Optional<T> read(final Path path) {
        worlds.keySet().removeIf(p -> !Files.isDirectory(p));
        final var normalized = path.toAbsolutePath().normalize();
        if (normalized.equals(plugin.getServer().getLevelDirectory().toAbsolutePath().normalize()))
            return Optional.empty();
        if (isWorld(normalized)) {
            final var data = worlds.computeIfAbsent(normalized, this::readWorld);
            return Optional.ofNullable(data);
        }
        worlds.remove(normalized);
        return Optional.empty();
    }

    protected Stream<Path> listCandidates(final Path path) throws IOException {
        return Files.list(path);
    }

    protected abstract boolean isWorld(Path path);

    protected abstract @Nullable T readWorld(Path path);
}
