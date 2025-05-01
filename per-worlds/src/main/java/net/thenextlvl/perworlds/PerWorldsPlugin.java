package net.thenextlvl.perworlds;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.thenextlvl.perworlds.command.WorldCommand;
import net.thenextlvl.perworlds.version.PluginVersionChecker;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public class PerWorldsPlugin extends JavaPlugin {
    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final SharedWorlds commons = new SharedWorlds(this);

    public PerWorldsPlugin() {
        registerCommands();
    }

    @Override
    public void onLoad() {
        versionChecker.checkVersion();
        commons.onLoad();
    }

    @Override
    public void onEnable() {
        commons.onEnable();
        warnWorldManager();
    }

    private void warnWorldManager() {
        if (commons.knownWorldManagers.stream()
                .filter(name -> !name.equals("Worlds"))
                .map(getServer().getPluginManager()::getPlugin)
                .noneMatch(Objects::nonNull)) return;
        getComponentLogger().warn("It appears you are using a third party world management plugin");
        getComponentLogger().warn("Please consider switching to 'Worlds' for first hand support");
        getComponentLogger().warn("Download at: https://hangar.papermc.io/TheNextLvl/Worlds");
        getComponentLogger().warn("Since Worlds already ships with PerWorlds, you have to uninstall this plugin when switching");
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
