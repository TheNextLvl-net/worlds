package net.thenextlvl.worlds.command;

import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.Dimension;
import net.thenextlvl.worlds.LegacyWorldRegistry;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@NullMarked
public final class UnimportedWorlds {
    private UnimportedWorlds() {
    }

    public static List<Entry> list(final WorldsPlugin plugin) {
        final var root = plugin.getServer().getWorldContainer().toPath();
        try (final var legacy = plugin.legacyWorldRegistry().listEntries(root);
             final var modern = plugin.modernWorldRegistry().listEntries(root)) {
            return Stream.concat(
                    legacy.map(entry -> new Entry(entry.getValue().key(), entry.getValue().dimension(), entry.getKey())),
                    modern.map(entry -> new Entry(entry.getValue().key(), null, entry.getKey()))
            ).toList();
        } catch (final IOException ignored) {
            return List.of();
        }
    }

    public static String importCommand(final WorldsPlugin plugin, final Path importPath, final Key key) {
        final var data = plugin.legacyWorldRegistry().read(importPath);
        return importCommand(plugin, importPath, key,
                data.map(LegacyWorldRegistry.LegacyWorldData::dimension).orElse(null),
                data.map(LegacyWorldRegistry.LegacyWorldData::generator).orElse(null),
                false);
    }

    public static String importCommand(final WorldsPlugin plugin, final Path importPath, final Key key,
                                       @Nullable final Dimension dimension, @Nullable final String generator,
                                       final boolean voidWorld) {
        final var root = plugin.getServer().getWorldContainer().toPath().toAbsolutePath().normalize();
        final var path = root.relativize(importPath.toAbsolutePath().normalize()).toString();
        final var command = new StringBuilder("world import ")
                .append(quote(path))
                .append(" ")
                .append(key.asString());
        if (dimension != null) command.append(" dimension ").append(dimension.key().asString());
        if (generator != null) command.append(" generator ").append(quote(generator));
        if (voidWorld) command.append(" --void-world");
        return command.toString();
    }

    private static String quote(final String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    public record Entry(Key key, @Nullable Dimension dimension, Path path) {
    }
}
