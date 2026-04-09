package net.thenextlvl.worlds.versions;

import net.thenextlvl.worlds.Level;
import net.thenextlvl.worlds.WorldsAccess;
import net.thenextlvl.worlds.experimental.DimensionType;
import net.thenextlvl.worlds.generator.Generator;
import net.thenextlvl.worlds.LevelStem;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.persistence.PersistentDataType.STRING;

public abstract class VersionHandler {
    private final @Nullable FoliaSupport foliaSupport;
    private final boolean foliaSupported;
    protected final PluginAccess plugin;

    public VersionHandler(final PluginAccess plugin, @Nullable final FoliaSupport foliaSupport, final boolean foliaSupported) {
        if (plugin.isRunningFolia() && !foliaSupported)
            throw new IllegalArgumentException("Folia is not supported in this version! Check for an update.");
        if (plugin.isRunningFolia() && foliaSupport == null)
            throw new IllegalArgumentException("Folia support is not properly implemented in this version! Check for an update.");
        if (!plugin.isRunningFolia() && foliaSupport != null) {
            throw new IllegalStateException("Folia support initialized on non-Folia server.");
        }
        this.plugin = plugin;
        this.foliaSupport = foliaSupport;
        this.foliaSupported = foliaSupported;
    }

    public abstract Class<?> getTickThreadClass();

    public abstract String getOverworldName();

    public abstract boolean isDirectoryLockException(Throwable throwable);

    public abstract CompletableFuture<@Nullable Void> saveAsync(World world, boolean flush);

    public abstract CompletableFuture<@Nullable Void> saveLevelDataAsync(World world);

    public abstract Optional<JavaPlugin> getGenerator(World world);

    public abstract void generateEndPlatform(World world, Entity entity);

    public abstract void handleEndCredits(Player player);

    public abstract @Nullable Location getRespawnLocation(Player player, boolean load);

    public abstract @Nullable Boolean hasBonusChest(final World world);

    public abstract CompletableFuture<World> createAsync(Level level);

    public abstract String findAvailableName(Path path, String name, String format) throws IOException;

    protected World.Environment toBukkit(final DimensionType type) {
        if (type.equals(DimensionType.OVERWORLD)) return World.Environment.NORMAL;
        if (type.equals(DimensionType.THE_NETHER)) return World.Environment.NETHER;
        if (type.equals(DimensionType.THE_END)) return World.Environment.THE_END;
        return World.Environment.CUSTOM;
    }

    protected void persistWorld(final World world, final LevelStem dimension, final boolean enabled) {
        final var worldKey = new NamespacedKey("worlds", "world_key");
        final var dimensionKey = new NamespacedKey("worlds", "dimension");
        world.getPersistentDataContainer().set(worldKey, STRING, world.key().asString());
        world.getPersistentDataContainer().set(dimensionKey, STRING, dimension.dimensionType().key().asString());
        WorldsAccess.access().setEnabled(world, enabled);
    }

    protected void persistGenerator(final World world, final Generator generator) {
        final var generatorKey = new NamespacedKey("worlds", "generator");
        world.getPersistentDataContainer().set(generatorKey, STRING, generator.asString());
    }

    public Optional<FoliaSupport> foliaSupport() {
        return Optional.ofNullable(foliaSupport);
    }

    public boolean isFoliaSupported() {
        return foliaSupported;
    }
}
