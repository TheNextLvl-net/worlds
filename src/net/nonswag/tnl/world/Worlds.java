package net.nonswag.tnl.world;

import net.nonswag.tnl.listener.NMSMain;
import net.nonswag.tnl.listener.api.command.CommandManager;
import net.nonswag.tnl.listener.utils.GlobalConfigUtil;
import net.nonswag.tnl.listener.utils.PluginUpdate;
import net.nonswag.tnl.world.api.enerators.VoidGenerator;
import net.nonswag.tnl.world.commands.WorldCommand;
import net.nonswag.tnl.world.listeners.WorldListener;
import net.nonswag.tnl.world.tabcompleter.WorldCommandTabCompleter;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

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
        if (Bukkit.getWorld(name) != null) {
            return;
        }
        try {
            if (new File(getPlugin().getDataFolder().getAbsoluteFile().getParentFile().getParent() + "/" + name).exists()) {
                try {
                    WorldType type = null;
                    ChunkGenerator generator = null;
                    World.Environment environment = null;
                    try {
                        type = ((WorldType) Worlds.getConfigUtil().get(name + ".type"));
                    } catch (Throwable ignored) {
                    }
                    try {
                        environment = World.Environment.valueOf(Worlds.getConfigUtil().getConfig().getString(name + ".environment"));
                    } catch (Throwable ignored) {
                    }
                    if (type == null) {
                        generator = new VoidGenerator();
                    }
                    WorldCreator worldCreator = new WorldCreator(name);
                    if (type != null) {
                        worldCreator.type(type);
                    }
                    if (environment != null) {
                        worldCreator.environment(environment);
                    }
                    if (generator != null) {
                        worldCreator.generator(generator);
                    }
                    worldCreator.createWorld();
                } catch (Throwable t) {
                    NMSMain.stacktrace(t);
                }
            }
        } catch (Throwable t) {
            NMSMain.stacktrace(t);
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

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return super.getDefaultWorldGenerator(worldName, id);
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        super.saveResource(resourcePath, replace);
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public PluginCommand getCommand(String name) {
        return super.getCommand(name);
    }

    @Override
    public Logger getLogger() {
        return super.getLogger();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return super.onTabComplete(sender, command, alias, args);
    }

    @Override
    public InputStream getResource(String filename) {
        return super.getResource(filename);
    }

    @Override
    protected File getFile() {
        return super.getFile();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(sender, command, label, args);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public FileConfiguration getConfig() {
        return super.getConfig();
    }
}
