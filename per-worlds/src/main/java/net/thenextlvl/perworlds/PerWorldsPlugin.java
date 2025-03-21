package net.thenextlvl.perworlds;

import net.thenextlvl.perworlds.version.PluginVersionChecker;
import org.bukkit.plugin.java.JavaPlugin;

public class PerWorldsPlugin extends JavaPlugin {
    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final SharedWorlds commons = new SharedWorlds(this);

    @Override
    public void onLoad() {
        versionChecker.checkVersion();
        commons.onLoad();
    }

    @Override
    public void onEnable() {
        commons.onEnable();
    }

    @Override
    public void onDisable() {
        commons.onDisable();
    }
}
