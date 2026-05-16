package net.thenextlvl.worlds;

import net.kyori.adventure.key.Key;
import net.thenextlvl.nbt.NBTInputStream;
import net.thenextlvl.nbt.tag.Tag;
import net.thenextlvl.worlds.view.PaperLevelView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

@NullMarked
public final class LegacyWorldRegistry extends UnimportedWorldRegistry<LegacyWorldRegistry.LegacyWorldData> {
    public LegacyWorldRegistry(final WorldsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWorld(final Path path) {
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

    @Override
    protected @Nullable LegacyWorldData readWorld(final Path path) {
        final var data = resolveLevelDat(path);
        if (data == null) return null;
        try (final var input = NBTInputStream.create(data)) {
            final var root = input.readTag().optional("Data").map(Tag::getAsCompound);

            final var name = root.flatMap(tag -> tag.optional("LevelName").map(Tag::getAsString))
                    .orElse(path.getFileName().toString());

            final var pdc = root.flatMap(tag -> tag.optional("BukkitValues").map(Tag::getAsCompound));

            final var enabled = pdc.flatMap(tag -> tag.optional("worlds:enabled").map(Tag::getAsBoolean)).orElse(false);
            final var key = pdc.flatMap(tag -> tag.optional("worlds:world_key").map(Tag::getAsString).map(Key::key))
                    .orElseGet(() -> plugin.levelView().findFreeKey("worlds", PaperLevelView.createKey(name)));

            final var dimension = pdc.flatMap(tag -> tag.optional("worlds:dimension").map(Tag::getAsString))
                    .map(this::dimension).orElse(null);
            final var generator = pdc.flatMap(tag -> tag.optional("worlds:generator").map(Tag::getAsString))
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

    public record LegacyWorldData(Key key, @Nullable Dimension dimension, boolean enabled, @Nullable String generator) {
    }
}
