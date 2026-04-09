package net.thenextlvl.worlds;

import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import dev.faststats.core.data.Metric;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.thenextlvl.i18n.ComponentBundle;
import net.thenextlvl.worlds.generator.GeneratorView;
import net.thenextlvl.worlds.command.SaveAllCommand;
import net.thenextlvl.worlds.command.SaveOffCommand;
import net.thenextlvl.worlds.command.SaveOnCommand;
import net.thenextlvl.worlds.command.SeedCommand;
import net.thenextlvl.worlds.command.WorldCommand;
import net.thenextlvl.worlds.command.WorldSetSpawnCommand;
import net.thenextlvl.worlds.level.LevelData;
import net.thenextlvl.worlds.link.WorldLinkProvider;
import net.thenextlvl.worlds.listener.PortalListener;
import net.thenextlvl.worlds.listener.TeleportListener;
import net.thenextlvl.worlds.listener.WorldListener;
import net.thenextlvl.worlds.model.MessageMigrator;
import net.thenextlvl.worlds.version.PluginVersionChecker;
import net.thenextlvl.worlds.versions.PluginAccess;
import net.thenextlvl.worlds.versions.VersionHandler;
import net.thenextlvl.worlds.view.FoliaLevelView;
import net.thenextlvl.worlds.view.PaperLevelView;
import org.bstats.bukkit.Metrics;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

@NullMarked
public final class WorldsPlugin extends JavaPlugin implements PluginAccess {
    public static final String ISSUES = "https://github.com/TheNextLvl-net/worlds/issues/new?template=bug_report.yml";
    public static final boolean RUNNING_FOLIA = ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));

    private final VersionHandler versionHandler = selectImplementation();

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware()
            .ignoreError(IllegalStateException.class, "World mismatch: expected .* but got .*")
            .ignoreErrorType(AccessDeniedException.class);

    private final GeneratorView generatorView = new net.thenextlvl.worlds.v4.generator.SimpleGeneratorView();
    private final PaperLevelView levelView = versionHandler.foliaSupport()
            .<PaperLevelView>map(support -> new FoliaLevelView(this, support))
            .orElseGet(() -> new PaperLevelView(this));

    private final WorldLinkProvider linkProvider = new WorldLinkProvider(this);

    private final Path presetsFolder = getDataPath().resolve("presets");
    private final Path translations = getDataPath().resolve("translations");
    private final Key key = Key.key("worlds", "translations");

    private final ComponentBundle bundle = ComponentBundle.builder(key, translations)
            .migrator(new MessageMigrator())
            .placeholder("prefix", "prefix")
            .resource("worlds.properties", Locale.US)
            .resource("worlds_german.properties", Locale.GERMANY)
            .build();

    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final BukkitMetrics fastStats = BukkitMetrics.factory()
            .token("978c4aa9ecf78ae2e9c0776601fd4c6c")
            .errorTracker(ERROR_TRACKER)
            .addMetric(addGeneratorChart())
            .addMetric(addWorldsChart())
            .addMetric(addEnvironmentsChart())
            .create(this);

    private final Metrics metrics = new Metrics(this, 19652);

    public WorldsPlugin() {
        getComponentLogger().info("Using implementation: {}", versionHandler.getClass().getName());
        registerCommands();
    }

    private VersionHandler selectImplementation() {
        final var s = ServerBuildInfo.buildInfo().minecraftVersionId();
        if (s.contains("1.21.4")) {
            return new net.thenextlvl.worlds.versions.v1_21_4.SimpleVersionHandler(this);
        } else if (s.contains("1.21.5") || s.contains("1.21.6") || s.contains("1.21.7") || s.contains("1.21.8")) {
            return new net.thenextlvl.worlds.versions.v1_21_8.SimpleVersionHandler(this);
        } else if (s.contains("1.21.9") || s.contains("1.21.10")) {
            return new net.thenextlvl.worlds.versions.v1_21_10.SimpleVersionHandler(this);
        } else if (s.contains("1.21.11")) {
            return new net.thenextlvl.worlds.versions.v1_21_11.SimpleVersionHandler(this);
        } else if (s.equals("26.1") || s.contains("26.1.1")) {
            return new net.thenextlvl.worlds.versions.v26_1_1.SimpleVersionHandler(this);
        }
        throw new IllegalStateException("No implementation found for version: " + s + ", check for an update.");
    }

    @Override
    public void onLoad() {
        createPresetsFolder();
        if (!RUNNING_FOLIA) checkPerWorldsRemnants();
        versionChecker.checkVersion();
        registerServices();
    }

    @Override
    public void onDisable() {
        linkProvider.persistTrees();
    }

    @Override
    public void onEnable() {
        fastStats.ready();
        warnVoidGeneratorPlugin();
        registerListeners();
    }

    private void checkPerWorldsRemnants() {
        if (getServer().getPluginManager().getPlugin("PerWorlds") != null) return;
        try (final var files = Files.list(Path.of("plugins", "PerWorlds", "groups"))) {
            if (files.noneMatch(path -> {
                return switch (path.getFileName().toString()) {
                    case "unowned", "unowned.dat", "unowned.dat_old" -> false;
                    default -> true;
                };
            })) return;
            getComponentLogger().warn("It looks like you have been using world groups before.");
            getComponentLogger().warn("World groups were provided by PerWorlds which is no longer inbuilt!");
            getComponentLogger().warn("If you want to continue using it you can download it from https://modrinth.com/project/lpfQmSV2");
        } catch (final IOException ignored) {
        }
    }

    private void warnVoidGeneratorPlugin() {
        final var names = Stream.of("VoidWorldGenerator", "VoidGen", "VoidGenerator", "VoidWorld", "VoidGenPlus",
                "DeluxeVoidWorld", "CleanroomGenerator", "CompletelyEmpty");
        if (names.map(getServer().getPluginManager()::getPlugin).filter(Objects::nonNull).findAny().isEmpty()) return;
        getComponentLogger().warn("It appears you are using a plugin to generate void worlds");
        getComponentLogger().warn("This is not required, and incompatible with Vanilla world generation");
        getComponentLogger().warn("Please use the preset 'the-void' instead");
        getComponentLogger().warn("You can do this with the command '/world create <key> preset the-void'");
        getComponentLogger().warn("Read more at https://thenextlvl.net/blog/void-generator-plugins");
    }

    public Path presetsFolder() {
        return presetsFolder;
    }

    public ComponentBundle bundle() {
        return bundle;
    }

    @Override
    public GeneratorView generatorView() {
        return generatorView;
    }

    @Override
    public Level.Builder levelBuilder(final Path directory) {
        return new LevelData.Builder(this, directory);
    }

    @Override
    public Level.Builder levelBuilder(final World world) {
        return levelView().read(world.getWorldFolder().toPath())
                .orElseGet(() -> levelBuilder(world.getWorldFolder().toPath()))
                .bonusChest(handler().hasBonusChest(world))
                .hardcore(world.isHardcore())
                .structures(world.canGenerateStructures())
                .worldKnown(true)
                .seed(world.getSeed())
                .biomeProvider(world.getBiomeProvider())
                .chunkGenerator(world.getGenerator())
                .key(world.key())
                .levelStem(switch (world.getEnvironment()) {
                    case NORMAL -> LevelStem.OVERWORLD;
                    case NETHER -> LevelStem.NETHER;
                    case THE_END -> LevelStem.END;
                    default -> null;
                })
                .seed(world.getSeed())
                .name(world.getName());
    }

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

    @Override
    public PaperLevelView levelView() {
        return levelView;
    }

    @Override
    public WorldLinkProvider linkProvider() {
        return linkProvider;
    }

    private void createPresetsFolder() {
        try {
            Files.createDirectories(presetsFolder);
        } catch (final IOException e) {
            getComponentLogger().warn("Failed to create presets folder", e);
        }
    }

    private void registerServices() {
        getServer().getServicesManager().register(WorldsProvider.class, this, this, ServicePriority.Highest);
    }

    private void registerListeners() {
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
            event.registrar().register(SeedCommand.create(this), "Query the seed of a world");
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

    private Metric<?> addEnvironmentsChart() {
        return Metric.stringArray("environments", () -> getServer().getWorlds().stream()
                .map(world -> switch (world.getEnvironment()) {
                    case NORMAL -> "Overworld";
                    case NETHER -> "Nether";
                    case THE_END -> "End";
                    case CUSTOM -> "Custom";
                })
                .toArray(String[]::new));
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
}
