package net.thenextlvl.perworlds;

import net.thenextlvl.perworlds.group.PaperGroupProvider;
import net.thenextlvl.perworlds.listener.WorldListener;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.util.Set;

public class SharedWorlds {
    public static final String ISSUES = "https://github.com/TheNextLvl-net/worlds/issues/new?template=bug_report.yml";
    private final PaperGroupProvider groupProvider;
    private final Metrics metrics;
    private final Plugin plugin;

    public SharedWorlds(Plugin plugin) {
        this.groupProvider = new PaperGroupProvider(plugin);
        this.metrics = new Metrics(plugin, 25295);
        this.plugin = plugin;
    }

    public void onLoad() {
        registerServices();
    }

    public void onEnable() {
        registerListeners();
        addCustomCharts();
    }

    public void onDisable() {
        groupProvider.getGroups().forEach(group -> {
            group.persistPlayerData();
            group.persist();
        });
        metrics.shutdown();
    }

    public GroupProvider groupProvider() {
        return groupProvider;
    }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new WorldListener(groupProvider), plugin);
    }

    private void registerServices() {
        plugin.getServer().getServicesManager().register(GroupProvider.class, groupProvider, plugin, ServicePriority.Highest);
    }

    private static final Set<String> knownWorldManagers = Set.of( // list ordered by likelihood of a plugin being used
            "Worlds",
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
                .filter(name -> plugin.getServer().getPluginManager().getPlugin(name) != null)
                .findAny().orElse("None")));
    }
}
