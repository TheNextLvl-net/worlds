package net.thenextlvl.perworlds;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.thenextlvl.perworlds.command.WorldCommand;
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
        registerCommands();
    }

    @Override
    public void onDisable() {
        commons.onDisable();
    }

    public SharedWorlds commons() {
        return commons;
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(WorldCommand.create(this)));
    }
}
