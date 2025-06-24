package net.thenextlvl.worlds;

import core.i18n.file.ComponentBundle;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupProvider;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.worlds.api.WorldsProvider;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.view.GeneratorView;
import net.thenextlvl.worlds.command.SaveAllCommand;
import net.thenextlvl.worlds.command.SeedCommand;
import net.thenextlvl.worlds.command.WorldCommand;
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
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

@NullMarked
public class WorldsPlugin extends JavaPlugin implements WorldsProvider {
    public static final boolean RUNNING_FOLIA = ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));

    private final GeneratorView generatorView = new PluginGeneratorView();
    private final PaperLevelView levelView = RUNNING_FOLIA ? new FoliaLevelView(this) : new PaperLevelView(this);

    private final WorldLinkProvider linkProvider = new WorldLinkProvider(this);

    private final @Nullable SharedWorlds commons = RUNNING_FOLIA ? null : new SharedWorlds(this);

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
        versionChecker.checkVersion();
        registerServices();
        if (commons != null) commons.onLoad();
    }

    @Override
    public void onDisable() {
        if (commons != null) commons.onDisable();
        linkProvider.persistTrees();
        metrics.shutdown();
    }

    @Override
    public void onEnable() {
        if (commons != null) commons.onEnable();
        warnVoidGeneratorPlugin();
        registerListeners();
    }

    private void warnVoidGeneratorPlugin() {
        var names = Stream.of("VoidWorldGenerator", "VoidGen", "VoidGenerator", "VoidWorld");
        if (names.map(getServer().getPluginManager()::getPlugin)
                .filter(Objects::nonNull).findAny().isEmpty()) return;
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
                .spawnChunkRadius(world.getGameRuleValue(GameRule.SPAWN_RADIUS))
                .key(world.getKey())
                .levelStem(switch (world.getEnvironment()) {
                    case NORMAL -> LevelStem.OVERWORLD;
                    case NETHER -> LevelStem.NETHER;
                    case THE_END -> LevelStem.END;
                    default -> null;
                })
                .seed(world.getSeed())
                .name(world.getName());
    }

    @Override
    public PaperLevelView levelView() {
        return levelView;
    }

    @Override
    public WorldLinkProvider linkProvider() {
        return linkProvider;
    }

    @Override
    public @Nullable GroupProvider groupProvider() {
        return commons != null ? commons.groupProvider() : null;
    }

    public @Nullable SharedWorlds commons() {
        return commons;
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
        getComponentLogger().warn("The PerWorlds module will NOT be enabled");
        getComponentLogger().warn("Please report any issues you encounter to {}", SharedWorlds.ISSUES);
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
            event.registrar().register(WorldCommand.create(this));
            event.registrar().register(SeedCommand.create(this));
            event.registrar().register(SaveAllCommand.create(this));
        }));
    }
}
