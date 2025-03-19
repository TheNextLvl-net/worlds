package net.thenextlvl.perworlds;

import net.thenextlvl.perworlds.group.PaperGroupProvider;
import net.thenextlvl.perworlds.listener.WorldListener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class SharedWorlds {
    private final JavaPlugin plugin;
    private final GroupProvider groupProvider = new PaperGroupProvider();

    public SharedWorlds(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void onLoad() {
        registerServices();
    }

    public void onEnable() {
        registerListeners();
        registerCommands();
    }

    private void registerCommands() {

    }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new WorldListener(), plugin);
    }

    private void registerServices() {
        plugin.getServer().getServicesManager().register(GroupProvider.class, groupProvider, plugin, ServicePriority.Highest);
    }

    public GroupProvider groupProvider() {
        return groupProvider;
    }
}
