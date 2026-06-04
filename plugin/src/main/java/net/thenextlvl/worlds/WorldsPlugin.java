package net.thenextlvl.worlds;

import dev.faststats.ErrorTracker;
import dev.faststats.bukkit.BukkitContext;
import dev.faststats.data.Metric;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.thenextlvl.binder.StaticBinder;
import net.thenextlvl.i18n.ComponentBundle;
import net.thenextlvl.worlds.command.SaveAllCommand;
import net.thenextlvl.worlds.command.SaveOffCommand;
import net.thenextlvl.worlds.command.SaveOnCommand;
import net.thenextlvl.worlds.command.WorldCommand;
import net.thenextlvl.worlds.command.WorldSetSpawnCommand;
import net.thenextlvl.worlds.event.WorldActionScheduledEvent;
import net.thenextlvl.worlds.event.WorldBackupEvent;
import net.thenextlvl.worlds.event.WorldBackupRestoreEvent;
import net.thenextlvl.worlds.event.WorldDeleteEvent;
import net.thenextlvl.worlds.event.WorldRegenerateEvent;
import net.thenextlvl.worlds.generator.GeneratorView;
import net.thenextlvl.worlds.listener.PluginListener;
import net.thenextlvl.worlds.listener.PortalListener;
import net.thenextlvl.worlds.listener.TeleportListener;
import net.thenextlvl.worlds.listener.WorldListener;
import net.thenextlvl.worlds.model.MessageMigrator;
import net.thenextlvl.worlds.version.PluginVersionChecker;
import net.thenextlvl.worlds.versions.PluginAccess;
import net.thenextlvl.worlds.versions.VersionHandler;
import net.thenextlvl.worlds.versions.v26_1_2.SimpleVersionHandler;
import net.thenextlvl.worlds.view.FoliaLevelView;
import net.thenextlvl.worlds.view.PaperLevelView;
import org.bstats.bukkit.Metrics;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@NullMarked
public final class WorldsPlugin extends JavaPlugin implements PluginAccess, WorldsAccess {
    public static final String ISSUES = "https://github.com/TheNextLvl-net/worlds/issues/new?template=bug_report.yml";
    public static final boolean RUNNING_FOLIA = ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));

    private final VersionHandler versionHandler = selectImplementation();

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware()
            .ignoreError(IllegalStateException.class, "World mismatch: expected .* but got .*")
            .ignoreError(AccessDeniedException.class);

    private final GeneratorView generatorView = GeneratorView.view();
    private final PaperLevelView levelView = versionHandler.foliaSupport()
            .<PaperLevelView>map(support -> new FoliaLevelView(this, support))
            .orElseGet(() -> new PaperLevelView(this));
    private final LegacyWorldRegistry legacyWorldRegistry = new LegacyWorldRegistry(this);
    private final ModernWorldRegistry modernWorldRegistry = new ModernWorldRegistry(this);
    private final SimpleWorldRegistry worldRegistry = new SimpleWorldRegistry(this);
    private final SimpleOperationScheduler worldOperationScheduler = new SimpleOperationScheduler(this);

    private BackupProvider backupProvider = new SimpleBackupProvider();

    private final Path presetsFolder = getDataPath().resolve("presets");
    private final Path translations = getDataPath().resolve("translations");
    private final Key key = Key.key("worlds", "translations");

    private final ComponentBundle bundle = ComponentBundle.builder(key, translations)
            .migrator(new MessageMigrator())
            .placeholder("prefix", "prefix")
            .resource("english.properties", Locale.US)
            .resource("german.properties", Locale.GERMANY)
            .build();

    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final BukkitContext context = new BukkitContext.Factory(this, "978c4aa9ecf78ae2e9c0776601fd4c6c")
            .errorTrackerService(ERROR_TRACKER)
            .metrics(factory -> factory
                    .addMetric(addGeneratorChart())
                    .addMetric(addWorldsChart())
                    .addMetric(addDimensionsChart())
                    .create())
            .create();

    private final Metrics metrics = new Metrics(this, 19652);

    public WorldsPlugin() {
        StaticBinder.getInstance(WorldsAccess.class.getClassLoader()).bind(WorldsAccess.class, this);
        getComponentLogger().info("Using implementation: {}", versionHandler.getClass().getName());
        registerCommands();
    }

    private VersionHandler selectImplementation() {
        final var s = ServerBuildInfo.buildInfo().minecraftVersionId();
        if (s.contains("26.1.2")) {
            return new SimpleVersionHandler(this);
        }
        throw new IllegalStateException("No implementation found for version: " + s + ", check for an update.");
    }

    @Override
    public void onLoad() {
        createPresetsFolder();
        versionChecker.checkVersion();
    }

    @Override
    public void onEnable() {
        context.ready();
        worldRegistry.read();
        worldOperationScheduler.load();
        worldOperationScheduler.runScheduledOperations();
        registerListeners();
    }

    @Override
    public void onDisable() {
        context.shutdown();
        metrics.shutdown();
    }

    public Path presetsFolder() {
        return presetsFolder;
    }

    public ComponentBundle bundle() {
        return bundle;
    }

    public GeneratorView generatorView() {
        return generatorView;
    }

    @NullUnmarked
    public <T> CompletableFuture<T> supplyGlobal(final Supplier<CompletableFuture<T>> supplier) {
        final var foliaTickThread = RUNNING_FOLIA && Thread.currentThread().getClass().equals(handler().getTickThreadClass());
        if (foliaTickThread || getServer().isGlobalTickThread()) try {
            return supplier.get();
        } catch (final Exception e) {
            return CompletableFuture.failedFuture(e);
        }
        final var future = new CompletableFuture<T>();
        getServer().getGlobalRegionScheduler().execute(this, () -> {
            supplier.get().thenAccept(future::complete).exceptionally(e -> {
                future.completeExceptionally(e);
                return null;
            });
        });
        return future;
    }

    public PaperLevelView levelView() {
        return levelView;
    }

    public LegacyWorldRegistry legacyWorldRegistry() {
        return legacyWorldRegistry;
    }

    public ModernWorldRegistry modernWorldRegistry() {
        return modernWorldRegistry;
    }

    private void createPresetsFolder() {
        try {
            Files.createDirectories(presetsFolder);
        } catch (final IOException e) {
            getComponentLogger().warn("Failed to create presets folder", e);
        }
    }

    private void registerListeners() {
        new PluginListener(this).init();
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        handler().foliaSupport().ifPresent(foliaSupport -> {
            final var foliaPortalListener = foliaSupport.createPortalListener();
            getServer().getPluginManager().registerEvents(foliaPortalListener, this);
        });
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            event.registrar().register(SaveAllCommand.create(this), "Save all worlds");
            event.registrar().register(SaveOffCommand.create(this), "Disable automatic world saving");
            event.registrar().register(SaveOnCommand.create(this), "Enable automatic world saving");
            event.registrar().register(WorldCommand.create(this), "The main command to interact with this plugin");
            event.registrar().register(WorldSetSpawnCommand.create(this, "setworldspawn"), "Set the world spawn");
        }));
    }

    private Metric<?> addGeneratorChart() {
        return Metric.stringArray("generators", () -> Arrays.stream(getServer().getPluginManager().getPlugins())
                .filter(Plugin::isEnabled)
                .filter(plugin -> generatorView().hasGenerator(plugin))
                .map(Plugin::getName)
                .toArray(String[]::new));
    }

    private Metric<?> addWorldsChart() {
        return Metric.number("worlds", () -> getServer().getWorlds().size());
    }

    private Metric<?> addDimensionsChart() {
        return Metric.numberMap("dimensions", () -> {
            final var dimensions = new HashMap<String, Integer>();
            for (final var world : getServer().getWorlds()) {
                final var dimension = getDimension(world);
                dimensions.compute(dimension.key().asString(), (key, value) -> value == null ? 1 : value + 1);
            }
            return dimensions;
        });
    }

    public VersionHandler handler() {
        return versionHandler;
    }

    @Override
    public ErrorTracker getErrorTracker() {
        return ERROR_TRACKER;
    }

    @Override
    public boolean isRunningFolia() {
        return RUNNING_FOLIA;
    }

    @Override
    public WorldRegistry getWorldRegistry() {
        return worldRegistry;
    }

    @Override
    public Stream<Dimension> customDimensions() {
        return handler().listDimensions();
    }

    @Override
    public Dimension getDimension(final World world) {
        return handler().getDimension(world);
    }

    @Override
    public Stream<Path> listLevels() {
        return levelView.listLevels();
    }

    @Override
    public CompletableFuture<World> load(final Key key) {
        return worldRegistry.get(key).map(entry -> levelView.read(key, entry))
                .map(Level.Builder::build)
                .map(Level::create)
                .orElseGet(() -> CompletableFuture.failedFuture(new WorldOperationException(
                        WorldOperationException.Reason.WORLD_NOT_FOUND
                ).key(key)));
    }

    @Override
    public CompletableFuture<World> create(final Level level) {
        return supplyGlobal(() -> handler().createAsync(level));
    }

    @Override
    public CompletableFuture<Boolean> unload(final World world, final boolean save) {
        return levelView.unloadAsync(world, save);
    }

    @Override
    public CompletableFuture<Boolean> save(final World world, final boolean flush) {
        return levelView.saveAsync(world, flush).thenApply(ignored -> true);
    }

    @Override
    public CompletableFuture<World> clone(final World world, final boolean full) {
        return clone(world, builder -> {
        }, full);
    }

    @Override
    public CompletableFuture<World> clone(final World world, final Consumer<Level.Builder> builder, final boolean full) {
        return levelView.cloneAsync(world, builder, full);
    }

    @Override
    public CompletableFuture<Boolean> delete(final World world) {
        return supplyGlobal(() -> deleteNow(world));
    }

    @Override
    public CompletableFuture<World> regenerate(final World world) {
        return regenerate(world, builder -> {
        });
    }

    @Override
    public CompletableFuture<World> regenerate(final World world, final Consumer<Level.Builder> builder) {
        return supplyGlobal(() -> regenerateNow(world, builder));
    }

    @Override
    public CompletableFuture<Backup> createBackup(final World world, @Nullable final String name) {
        return supplyGlobal(() -> {
            new WorldBackupEvent(world).callEvent();
            return save(world, true).thenCompose(ignored -> getBackupProvider().backup(world, name));
        });
    }

    @Override
    public CompletableFuture<World> restoreBackup(final World world, final Backup backup) {
        return supplyGlobal(() -> {
            if (levelView.isOverworld(world)) return CompletableFuture.failedFuture(new WorldOperationException(
                    WorldOperationException.Reason.BACKUP_RESTORE_REQUIRES_SCHEDULING
            ));
            if (!new WorldBackupRestoreEvent(world, backup).callEvent())
                return CompletableFuture.failedFuture(new WorldOperationException(
                        WorldOperationException.Reason.EVENT_CANCELLED
                ));
            final var players = List.copyOf(world.getPlayers());
            return movePlayersToOverworld(world).thenCompose(ignored -> getBackupProvider().restore(world, backup)
                    .thenApply(restored -> {
                        players.forEach(player -> player.teleportAsync(
                                restored.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN
                        ));
                        return restored;
                    }).exceptionallyCompose(throwable -> {
                        final var t = throwable.getCause() != null ? throwable.getCause() : throwable;
                        final var level = Level.copy(world).build();
                        return level.create().thenCompose(restored -> {
                            players.forEach(player -> player.teleportAsync(
                                    restored.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN
                            ));
                            return CompletableFuture.failedFuture(t);
                        });
                    }));
        });
    }

    private CompletableFuture<Boolean> deleteNow(final World world) {
        if (levelView.isOverworld(world)) return CompletableFuture.failedFuture(new WorldOperationException(
                WorldOperationException.Reason.DELETE_REQUIRES_SCHEDULING
        ));
        if (!new WorldDeleteEvent(world).callEvent()) return CompletableFuture.failedFuture(new WorldOperationException(
                WorldOperationException.Reason.EVENT_CANCELLED
        ));

        return movePlayersToOverworld(world).thenCompose(ignored -> unload(world, false).thenCompose(success -> {
            if (!success) return CompletableFuture.failedFuture(new WorldOperationException(
                    WorldOperationException.Reason.UNLOAD_FAILED
            ).key(world.key()));
            levelView.delete(world.getWorldPath(), world.key());
            worldRegistry.unregister(world.key());
            getScheduler().cancel(world.key());
            return CompletableFuture.completedFuture(true);
        })).exceptionallyCompose(throwable -> {
            final var t = throwable.getCause() != null ? throwable.getCause() : throwable;
            if (t instanceof WorldOperationException) return CompletableFuture.failedFuture(t);
            getComponentLogger().warn("Failed to delete world", throwable);
            return CompletableFuture.failedFuture(new WorldOperationException(
                    WorldOperationException.Reason.INTERNAL_ERROR,
                    t
            ).key(world.key()).key(world.key()));
        });
    }

    private CompletableFuture<World> regenerateNow(final World world, final Consumer<Level.Builder> consumer) {
        if (levelView.isOverworld(world)) return CompletableFuture.failedFuture(new WorldOperationException(
                WorldOperationException.Reason.REGENERATE_REQUIRES_SCHEDULING
        ));
        if (!new WorldRegenerateEvent(world).callEvent())
            return CompletableFuture.failedFuture(new WorldOperationException(
                    WorldOperationException.Reason.EVENT_CANCELLED
            ));

        final var players = world.getPlayers();
        return movePlayersToOverworld(world).thenCompose(ignored -> unload(world, false).thenCompose(success -> {
            if (!success) return CompletableFuture.failedFuture(new WorldOperationException(
                    WorldOperationException.Reason.UNLOAD_FAILED
            ).key(world.key()));

            final var builder = Level.copy(world).resetSpawnPosition(true);
            consumer.accept(builder);
            final var level = builder.build();

            levelView.regenerate(world.getWorldPath(), level.getSeed());
            getScheduler().cancel(world.key(), WorldActionScheduledEvent.ActionType.REGENERATE);

            return level.create().thenApply(regenerated -> {
                players.forEach(player -> player.teleportAsync(
                        regenerated.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN
                ));
                return regenerated;
            });
        }));
    }

    private CompletableFuture<Void> movePlayersToOverworld(final World world) {
        final var fallback = levelView.getOverworld().getSpawnLocation();
        return CompletableFuture.allOf(world.getPlayers().stream()
                .map(player -> player.teleportAsync(fallback, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(success -> {
                    if (!success) player.kick(bundle().component("world.unload.kicked", player));
                }))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public String getEntryPermission(final World world) {
        return "worlds.enter." + world.key().asString();
    }

    @Override
    public OperationScheduler getScheduler() {
        return worldOperationScheduler;
    }

    @Override
    public BackupProvider getBackupProvider() {
        return backupProvider;
    }

    @Override
    public void setBackupProvider(final BackupProvider provider) {
        this.backupProvider = provider;
    }

    @Override
    public Optional<World> getPortalTarget(final World world, final PortalType type) {
        return levelView.getTarget(world, type);
    }

    @Override
    public Path getDimensionsRoot() {
        return getServer().getLevelDirectory().resolve("dimensions");
    }

    @Override
    public Path resolveLevelDirectory(final Key key) {
        return getDimensionsRoot().resolve(key.namespace()).resolve(key.value());
    }
}
