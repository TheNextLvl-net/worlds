package net.thenextlvl.worlds;

import core.i18n.file.ComponentBundle;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupProvider;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.worlds.api.WorldsProvider;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.view.GeneratorView;
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
import org.bukkit.NamespacedKey;
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

import static org.bukkit.persistence.PersistentDataType.BOOLEAN;
import static org.bukkit.persistence.PersistentDataType.STRING;

@NullMarked
public class WorldsPlugin extends JavaPlugin implements WorldsProvider {
    private final boolean runningFolia = ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));

    private final GeneratorView generatorView = new PluginGeneratorView();
    private final PaperLevelView levelView = runningFolia ? new FoliaLevelView(this) : new PaperLevelView(this);

    private final WorldLinkProvider linkProvider = new WorldLinkProvider(this);

    private final @Nullable SharedWorlds commons = runningFolia ? null : new SharedWorlds(this);

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
        if (runningFolia) warnExperimental();
        versionChecker.checkVersion();
        registerServices();
        if (commons != null) commons.onLoad();
    }

    @Override
    public void onDisable() {
        if (commons != null) commons.onDisable();
        metrics.shutdown();
        unloadLevels();
    }

    @Override
    public void onEnable() {
        if (isRunningFolia()) {
            getComponentLogger().error("Folia 1.21.5 is not yet supported by Worlds");
            getComponentLogger().error("Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
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

    public void persistWorld(World world, boolean enabled) {
        var worldKey = new NamespacedKey("worlds", "world_key");
        world.getPersistentDataContainer().set(worldKey, STRING, world.getKey().asString());
        persistStatus(world, enabled, true);
    }

    public void persistStatus(World world, boolean enabled, boolean force) {
        var enabledKey = new NamespacedKey("worlds", "enabled");
        if (!force && !world.getPersistentDataContainer().has(enabledKey)) return;
        world.getPersistentDataContainer().set(enabledKey, BOOLEAN, enabled);
    }

    public void persistGenerator(World world, Generator generator) {
        var generatorKey = new NamespacedKey("worlds", "generator");
        world.getPersistentDataContainer().set(generatorKey, STRING, generator.serialize());
    }

    public boolean isRunningFolia() {
        return runningFolia;
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

    private void unloadLevels() {
        getServer().getWorlds().forEach(linkProvider()::persistTree);
        getServer().getWorlds().stream().filter(world -> !world.isAutoSave()).forEach(world -> {
            world.getPlayers().forEach(player -> player.kick(getServer().shutdownMessage()));
            levelView().unloadLevel(world, false);
        });
    }

    private void registerServices() {
        getServer().getServicesManager().register(WorldsProvider.class, this, this, ServicePriority.Highest);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event ->
                event.registrar().register(WorldCommand.create(this))));
    }

    private void saveDefaultPresets() {
        Presets.BOTTOMLESS_PIT.saveToFile(new File(presetsFolder, "bottomless-pit.json"), true);
        Presets.CLASSIC_FLAT.saveToFile(new File(presetsFolder, "classic-flat.json"), true);
        Presets.DESERT.saveToFile(new File(presetsFolder, "desert.json"), true);
        Presets.OVERWORLD.saveToFile(new File(presetsFolder, "overworld.json"), true);
        Presets.REDSTONE_READY.saveToFile(new File(presetsFolder, "redstone-ready.json"), true);
        Presets.SNOWY_KINGDOM.saveToFile(new File(presetsFolder, "snowy-kingdom.json"), true);
        Presets.THE_VOID.saveToFile(new File(presetsFolder, "the-void.json"), true);
        Presets.TUNNELERS_DREAM.saveToFile(new File(presetsFolder, "tunnelers-dream.json"), true);
        Presets.WATER_WORLD.saveToFile(new File(presetsFolder, "water-world.json"), true);
    }
}
