package net.thenextlvl.worlds;

import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.view.PaperLevelView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@NullMarked
public final class ModernWorldRegistry extends UnimportedWorldRegistry<ModernWorldRegistry.ModernWorldData> {
    public ModernWorldRegistry(final WorldsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected Stream<Path> listCandidates(final Path path) {
        final var loaded = plugin.getServer().getWorlds().stream()
                .map(world -> world.getWorldPath().toAbsolutePath().normalize())
                .toList();
        final var managed = plugin.listLevels()
                .map(level -> level.toAbsolutePath().normalize())
                .toList();
        return plugin.levelView().listLevelFolders()
                .map(level -> level.toAbsolutePath().normalize())
                .filter(level -> !loaded.contains(level))
                .filter(level -> !managed.contains(level));
    }

    @Override
    protected boolean isWorld(final Path path) {
        final var normalized = path.toAbsolutePath().normalize();
        return Files.isDirectory(normalized.resolve("region"))
                && isUnloaded(normalized)
                && isUnmanaged(normalized);
    }

    @Override
    protected @Nullable ModernWorldData readWorld(final Path path) {
        final var normalized = path.toAbsolutePath().normalize();
        return key(normalized)
                .map(key -> new ModernWorldData(key, isKeyImportable(normalized)))
                .orElse(null);
    }

    @SuppressWarnings("PatternValidation")
    private Optional<Key> key(final Path path) {
        return plugin.levelView().lenientKey(path).or(() -> Optional.ofNullable(path.getFileName())
                .map(Path::toString)
                .map(PaperLevelView::createKey)
                .filter(value -> !value.isBlank())
                .map(value -> plugin.levelView().findFreeKey("worlds", value)));
    }

    private boolean isKeyImportable(final Path path) {
        return plugin.levelView().key(path)
                .map(plugin::resolveLevelDirectory)
                .map(level -> level.toAbsolutePath().normalize())
                .filter(path::equals)
                .isPresent();
    }

    private boolean isUnloaded(final Path path) {
        return plugin.getServer().getWorlds().stream()
                .map(world -> world.getWorldPath().toAbsolutePath().normalize())
                .noneMatch(path::equals);
    }

    private boolean isUnmanaged(final Path path) {
        return plugin.listLevels()
                .map(level -> level.toAbsolutePath().normalize())
                .noneMatch(path::equals);
    }

    public record ModernWorldData(Key key, boolean keyImportable) {
    }
}
