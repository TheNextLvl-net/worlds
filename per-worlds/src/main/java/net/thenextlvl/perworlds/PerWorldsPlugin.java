package net.thenextlvl.perworlds;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.thenextlvl.perworlds.command.WorldCommand;
import net.thenextlvl.perworlds.version.PluginVersionChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.Set;

@NullMarked
public class PerWorldsPlugin extends JavaPlugin {
    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final SharedWorlds commons = new SharedWorlds(this);
    private final Metrics metrics = new Metrics(this, 25295);

    public PerWorldsPlugin() {
        registerCommands();
    }

    @Override
    public void onLoad() {
        versionChecker.checkVersion();
        addCustomCharts();
        commons.onLoad();
    }

    @Override
    public void onEnable() {
        commons.onEnable();
        warnWorldManager();
    }

    @Override
    public void onDisable() {
        commons.onDisable();
        metrics.shutdown();
    }

    private void warnWorldManager() {
        if (knownWorldManagers.stream()
                .map(getServer().getPluginManager()::getPlugin)
                .noneMatch(Objects::nonNull)) return;
        getComponentLogger().warn("It appears you are using a third party world management plugin");
        getComponentLogger().warn("Please consider switching to 'Worlds' for first hand support");
        getComponentLogger().warn("Download at: https://modrinth.com/project/gBIw3Gvy");
        getComponentLogger().warn("Since Worlds already ships with PerWorlds, you have to uninstall this plugin when switching");
    }

    public SharedWorlds commons() {
        return commons;
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(WorldCommand.create(this)));
    }

    final Set<String> knownWorldManagers = Set.of( // list ordered by likelihood of a plugin being used
            "Multiverse-Core", // https://github.com/Multiverse/Multiverse-Core/
            "MultiWorld", // https://dev.bukkit.org/projects/multiworld-v-2-0 // https://modrinth.com/plugin/multiworld-bukkit
            "PhantomWorlds", // https://github.com/TheNewEconomy/PhantomWorlds
            "Hyperverse", // https://github.com/Incendo/Hyperverse
            "LightWorlds", // https://github.com/justin0-0/LightWorlds
            "SolarSystem", // https://github.com/OneLiteFeatherNET/SolarSystemPlugin
            "MoreFoWorld", // https://github.com/Folia-Inquisitors/MoreFoWorld
            "WorldManager", // https://www.spigotmc.org/resources/worldmanager-1-8-1-18-free-download-api.101875/
            "WorldMaster", // https://www.spigotmc.org/resources/worldmaster.101171/
            "TheGalaxyLimits", // https://hangar.papermc.io/TheGlitchedVirus/thegalaxylimits
            "BulMultiverse", // https://github.com/BulPlugins/BulMultiverse
            "worldmgr" // https://dev.bukkit.org/projects/worldmgr
    );

    private void addCustomCharts() {
        metrics.addCustomChart(new SimplePie("world_management_plugin", () -> knownWorldManagers.stream()
                .filter(name -> getServer().getPluginManager().getPlugin(name) != null)
                .findAny().orElse("None")));
    }
}
