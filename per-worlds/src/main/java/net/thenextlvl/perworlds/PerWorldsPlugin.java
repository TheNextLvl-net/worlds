package net.thenextlvl.perworlds;

import net.thenextlvl.perworlds.version.PluginVersionChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class PerWorldsPlugin extends JavaPlugin {
    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final Metrics metrics = new Metrics(this, 19652); // todo: create project

    @Override
    public void onLoad() {
        versionChecker.checkVersion();
    }

    @Override
    public void onDisable() {
        metrics.shutdown();
    }
}
