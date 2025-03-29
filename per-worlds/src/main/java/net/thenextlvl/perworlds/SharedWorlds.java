package net.thenextlvl.perworlds;

import net.thenextlvl.perworlds.group.PaperGroupProvider;
import net.thenextlvl.perworlds.listener.WorldListener;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

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

    private void addCustomCharts() {
        metrics.addCustomChart(new SimplePie("using_worlds", () -> {
            var worlds = plugin.getServer().getPluginManager().getPlugin("Worlds") != null;
            return String.valueOf(worlds);
        }));
    }
}
