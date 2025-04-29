package net.thenextlvl.worlds;

import core.i18n.file.ComponentBundle;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupProvider;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.worlds.api.WorldsProvider;
import net.thenextlvl.worlds.api.link.LinkController;
import net.thenextlvl.worlds.api.model.Generator;
import net.thenextlvl.worlds.api.model.LevelBuilder;
import net.thenextlvl.worlds.api.preset.Presets;
import net.thenextlvl.worlds.api.view.GeneratorView;
import net.thenextlvl.worlds.api.view.LevelView;
import net.thenextlvl.worlds.command.WorldCommand;
import net.thenextlvl.worlds.controller.WorldLinkController;
import net.thenextlvl.worlds.listener.PortalListener;
import net.thenextlvl.worlds.listener.ServerListener;
import net.thenextlvl.worlds.listener.WorldListener;
import net.thenextlvl.worlds.model.MessageMigrator;
import net.thenextlvl.worlds.model.PaperLevelBuilder;
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

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

import static org.bukkit.persistence.PersistentDataType.BOOLEAN;
import static org.bukkit.persistence.PersistentDataType.STRING;

@NullMarked
public class WorldsPlugin extends JavaPlugin implements WorldsProvider {
    private final boolean runningFolia = ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));

    private final GeneratorView generatorView = new PluginGeneratorView();
    private final LevelView levelView = runningFolia ? new FoliaLevelView(this) : new PaperLevelView(this);

    private final LinkController linkController = new WorldLinkController(this);

    private final @Nullable SharedWorlds commons = runningFolia ? null : new SharedWorlds(this);

    private final File presetsFolder = new File(getDataFolder(), "presets");
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
        if (!presetsFolder.isDirectory()) saveDefaultPresets();
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
        if (commons != null) commons.onEnable();
        registerListeners();
    }

    public File presetsFolder() {
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
    public LevelBuilder levelBuilder(File level) {
        return new PaperLevelBuilder(this, level);
    }

    @Override
    public LevelView levelView() {
        return levelView;
    }

    @Override
    public LinkController linkController() {
        return linkController;
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

    private void warnExperimental() {
        getComponentLogger().warn("Folia builds of Worlds are extremely experimental");
        getComponentLogger().warn("The PerWorlds module will NOT be enabled");
        getComponentLogger().warn("Please report any issues you encounter to {}", SharedWorlds.ISSUES);
    }

    private void unloadLevels() {
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
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
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
