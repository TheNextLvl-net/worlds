package net.nonswag.tnl.world;

import net.nonswag.tnl.listener.api.command.CommandManager;
import net.nonswag.tnl.listener.utils.GlobalConfigUtil;
import net.nonswag.tnl.listener.utils.PluginUpdate;
import net.nonswag.tnl.world.api.Environment;
import net.nonswag.tnl.world.commands.WorldCommand;
import net.nonswag.tnl.world.listeners.WorldListener;
import net.nonswag.tnl.world.tabcompleter.WorldCommandTabCompleter;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class Worlds extends JavaPlugin {

    private static Plugin plugin;
    private static JavaPlugin javaPlugin;
    private static GlobalConfigUtil configUtil;

    @Override
    public void onEnable() {
        super.onEnable();
        setPlugin(this);
        setJavaPlugin(this);
        setConfigUtil(new GlobalConfigUtil(getPlugin()));
        getConfigUtil().initConfig();
        Bukkit.getPluginManager().registerEvents(new WorldListener(), getPlugin());
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand("world", "tnl.world", new WorldCommand(), new WorldCommandTabCompleter());
        loadWorlds();
        new PluginUpdate(getPlugin()).downloadUpdate();
    }

    public static String getWorld(String name) {
        for(String s : getWorlds()) {
            String[] split = s.split("/");
            if (s.equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    public static void loadWorlds() {
        for(String s : getWorlds()) {
            String[] split = s.split("/");
            if (Bukkit.getWorld(split[split.length - 1]) == null) {
                loadWorld(split[split.length - 1]);
            }
        }
    }

    public static void loadWorld(String name) {
        if (Bukkit.getWorld(name) == null) {
            if (new File(getPlugin().getDataFolder().getAbsoluteFile().getParentFile().getParent() + "/" + name).exists()) {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(Worlds.getConfigUtil().getConfig().getString(name + ".generator"));
                WorldType type = WorldType.getByName(Worlds.getConfigUtil().getConfig().getString(name + ".type"));
                Environment environment = Environment.getByName(Worlds.getConfigUtil().getConfig().getString(name + ".environment"));
                ChunkGenerator generator = plugin.getDefaultWorldGenerator(name, null);
                WorldCreator worldCreator = new WorldCreator(name);
                worldCreator.createWorld();
            }
        }
    }

    public static List<String> getWorlds() {
        return Worlds.getConfigUtil().getConfig().getStringList("worlds");
    }

    public static GlobalConfigUtil getConfigUtil() {
        return configUtil;
    }

    public static void setConfigUtil(GlobalConfigUtil configUtil) {
        Worlds.configUtil = configUtil;
    }

    public static void setPlugin(Plugin plugin) {
        Worlds.plugin = plugin;
    }

    public static void setJavaPlugin(JavaPlugin javaPlugin) {
        Worlds.javaPlugin = javaPlugin;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }
}
