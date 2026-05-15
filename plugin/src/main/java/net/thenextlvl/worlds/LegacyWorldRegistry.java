package net.thenextlvl.worlds;

import net.kyori.adventure.key.Key;
import net.thenextlvl.nbt.NBTInputStream;
import net.thenextlvl.nbt.tag.Tag;
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
public final class LegacyWorldRegistry {
    private final Map<Path, LegacyWorldData> worlds = new ConcurrentHashMap<>();
    private final WorldsPlugin plugin;

    public LegacyWorldRegistry(final WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    public Stream<Path> listPaths(final Path path) throws IOException {
        return listEntries(path).map(Map.Entry::getKey);
    }

    @SuppressWarnings("resource")
    public Stream<Map.Entry<Path, LegacyWorldData>> listEntries(final Path path) throws IOException {
        return Files.list(path)
                .map(path1 -> read(path1).map(data -> Map.entry(path1, data)).orElse(null))
                .filter(Objects::nonNull);
    }

    public Optional<LegacyWorldData> read(final Path path) {
        worlds.keySet().removeIf(p -> !Files.isDirectory(p));
        final var normalized = path.toAbsolutePath().normalize();
        if (isLegacyWorld(normalized)) {
            final var legacyWorldData = worlds.computeIfAbsent(normalized, this::readLegacyWorld);
            return Optional.ofNullable(legacyWorldData);
        }
        worlds.remove(normalized);
        return Optional.empty();
    }

    private boolean isLegacyWorld(final Path path) {
        return Files.isRegularFile(path.resolve("level.dat"))
                || Files.isRegularFile(path.resolve("level.dat_old"));
    }

    private @Nullable Path resolveLevelDat(final Path path) {
        final var levelDat = path.resolve("level.dat");
        if (Files.isRegularFile(levelDat)) return levelDat;
        final var levelDatOld = path.resolve("level.dat_old");
        if (Files.isRegularFile(levelDatOld)) return levelDatOld;
        return null;
    }

    private @Nullable LegacyWorldData readLegacyWorld(final Path path) {
        final var data = resolveLevelDat(path);
        if (data == null) return null;
        try (final var input = NBTInputStream.create(data)) {
            final var pdc = input.readTag()
                    .optional("Data").map(Tag::getAsCompound)
                    .flatMap(tag -> tag.optional("BukkitValues").map(Tag::getAsCompound))
                    .orElse(null);
            if (pdc == null) return null;

            final var enabled = pdc.optional("worlds:enabled").map(Tag::getAsBoolean).orElse(null);
            if (enabled == null) return null;

            final var key = pdc.optional("worlds:world_key").map(Tag::getAsString).map(Key::key).orElse(null);
            if (key == null) return null;

            final var dimension = pdc.optional("worlds:dimension")
                    .map(Tag::getAsString)
                    .map(this::dimension)
                    .orElse(Dimension.OVERWORLD);
            final var generator = pdc.optional("worlds:generator")
                    .map(Tag::getAsString)
                    .orElse(null);
            return new LegacyWorldData(key, dimension, enabled, generator);
        } catch (final Exception e) {
            plugin.getComponentLogger().warn("Failed to read legacy world data from {}", path, e);
            return null;
        }
    }

    private @Nullable Dimension dimension(final String key) {
        return switch (key) {
            case "minecraft:overworld" -> Dimension.OVERWORLD;
            case "minecraft:the_end" -> Dimension.THE_END;
            case "minecraft:the_nether" -> Dimension.THE_NETHER;
            default -> null;
        };
    }

    public record LegacyWorldData(Key key, Dimension dimension, boolean enabled, @Nullable String generator) {
    }
}
