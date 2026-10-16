package net.thenextlvl.worlds.versions;

import net.thenextlvl.worlds.Dimension;
import net.thenextlvl.worlds.Level;
import net.thenextlvl.worlds.generator.GeneratorType;
import org.bukkit.FeatureFlag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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

    public abstract boolean isDirectoryLockException(Throwable throwable);

    public abstract CompletableFuture<@Nullable Void> saveAsync(World world, boolean flush);

    public abstract CompletableFuture<@Nullable Void> saveLevelDataAsync(World world);

    public abstract Optional<JavaPlugin> getGenerator(World world);

    public abstract void generateEndPlatform(World world, Entity entity);

    public abstract void handleEndCredits(Player player);

    public abstract @Nullable Location getRespawnLocation(Player player, boolean load);

    public abstract CompletableFuture<World> createAsync(Level level);

    public abstract String findAvailableName(Path path, String name, String format) throws IOException;

    public abstract GeneratorType getGeneratorType(World world);

    public abstract Stream<Dimension> listDimensions();

    public abstract Dimension getDimension(World world);

    public abstract void warnAndDelayStartupMigration();

    public abstract Set<FeatureFlag> getEnabledFeatures();

    public Optional<FoliaSupport> foliaSupport() {
        return Optional.ofNullable(foliaSupport);
    }

    public boolean isFoliaSupported() {
        return foliaSupported;
    }
}
