package net.thenextlvl.worlds;

import core.i18n.file.ComponentBundle;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.api.WorldsProvider;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.view.GeneratorView;
import net.thenextlvl.worlds.command.SaveAllCommand;
import net.thenextlvl.worlds.command.SaveOffCommand;
import net.thenextlvl.worlds.command.SaveOnCommand;
import net.thenextlvl.worlds.command.SeedCommand;
import net.thenextlvl.worlds.command.WorldCommand;
import net.thenextlvl.worlds.command.WorldSetSpawnCommand;
import net.thenextlvl.worlds.level.LevelData;
import net.thenextlvl.worlds.link.WorldLinkProvider;
import net.thenextlvl.worlds.listener.PortalListener;
import net.thenextlvl.worlds.listener.WorldListener;
import net.thenextlvl.worlds.model.MessageMigrator;
import net.thenextlvl.worlds.version.PluginVersionChecker;
import net.thenextlvl.worlds.view.FoliaLevelView;
import net.thenextlvl.worlds.view.PaperLevelView;
import net.thenextlvl.worlds.view.PluginGeneratorView;
import org.bstats.bukkit.Metrics;
import org.bukkit.World;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

@NullMarked
public class WorldsPlugin extends JavaPlugin implements WorldsProvider {
    public static final String ISSUES = "https://github.com/TheNextLvl-net/worlds/issues/new?template=bug_report.yml";
    public static final boolean RUNNING_FOLIA = ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));

    private final GeneratorView generatorView = new PluginGeneratorView();
    private final PaperLevelView levelView = RUNNING_FOLIA ? new FoliaLevelView(this) : new PaperLevelView(this);

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
    private final Metrics metrics = new Metrics(this, 19652);

    public WorldsPlugin() {
        registerCommands();
    }

    @Override
    public void onLoad() {
        createPresetsFolder();
        if (RUNNING_FOLIA) warnExperimental();
        else checkPerWorldsRemnants();
        versionChecker.checkVersion();
        registerServices();
    }

    private void checkPerWorldsRemnants() {
        if (getServer().getPluginManager().getPlugin("PerWorlds") != null) return;
        try (var files = Files.list(Path.of("plugins", "PerWorlds", "groups"))) {
            if (files.noneMatch(path -> {
                return switch (path.getFileName().toString()) {
                    case "unowned", "unowned.dat", "unowned.dat_old" -> false;
                    default -> true;
                };
            })) return;
            getComponentLogger().warn("It looks like you have been using world groups before.");
            getComponentLogger().warn("World groups were provided by PerWorlds which is no longer inbuilt!");
            getComponentLogger().warn("If you want to continue using it you can download it from https://modrinth.com/project/lpfQmSV2");
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onDisable() {
        linkProvider.persistTrees();
    }

    @Override
    public void onEnable() {
        warnVoidGeneratorPlugin();
        registerListeners();
    }

    private void warnVoidGeneratorPlugin() {
        var names = Stream.of("VoidWorldGenerator", "VoidGen", "VoidGenerator", "VoidWorld", "VoidGenPlus", "DeluxeVoidWorld");
        if (names.map(getServer().getPluginManager()::getPlugin).filter(Objects::nonNull).findAny().isEmpty()) return;
        getComponentLogger().warn("It appears you are using a plugin to generate void worlds");
        getComponentLogger().warn("This is not required, and incompatible with Vanilla world generation");
        getComponentLogger().warn("Please use the preset 'the-void' instead");
        getComponentLogger().warn("You can do this with the command '/world create <key> preset the-void'");
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
    public Level.Builder levelBuilder(Path directory) {
        return new LevelData.Builder(this, directory);
    }

    @Override
    public Level.Builder levelBuilder(World world) {
        return levelView().read(world.getWorldFolder().toPath())
                .orElseGet(() -> levelBuilder(world.getWorldFolder().toPath()))
                .bonusChest(world.hasBonusChest())
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

    public <T> CompletableFuture<T> supplyGlobal(Supplier<CompletableFuture<T>> supplier) {
        var future = new CompletableFuture<T>();
        if (getServer().isGlobalTickThread()) {
            try {
                future.complete(supplier.get().join());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        } else getServer().getGlobalRegionScheduler().execute(this, () -> {
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
        } catch (IOException e) {
            getComponentLogger().warn("Failed to create presets folder", e);
        }
    }

    private void warnExperimental() {
        getComponentLogger().warn("Folia builds of Worlds are extremely experimental");
        getComponentLogger().warn("Please report any issues you encounter to {}", ISSUES);
    }

    private void registerServices() {
        getServer().getServicesManager().register(WorldsProvider.class, this, this, ServicePriority.Highest);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
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
}
