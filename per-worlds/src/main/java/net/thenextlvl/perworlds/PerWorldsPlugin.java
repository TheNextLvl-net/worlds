package net.thenextlvl.perworlds;

import net.thenextlvl.perworlds.version.PluginVersionChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

public class PerWorldsPlugin extends JavaPlugin {
    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final Metrics metrics = new Metrics(this, 19652); // todo: create project

    @Override
    public void onLoad() {
        versionChecker.checkVersion();
    }

    @Override
    public void onEnable() {
        addCustomCharts();
    }

    @Override
    public void onDisable() {
        metrics.shutdown();
    }

    private void addCustomCharts() {
        metrics.addCustomChart(new SimplePie("worlds", () -> {
            var worlds = getServer().getPluginManager().getPlugin("Worlds") != null;
            return String.valueOf(worlds);
        }));
    }
}
