package net.nonswag.tnl.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.nonswag.tnl.listener.api.command.CommandManager;
import net.nonswag.tnl.listener.api.file.JsonConfig;
import net.nonswag.tnl.listener.api.plugin.PluginUpdate;
import net.nonswag.tnl.listener.api.settings.Settings;
import net.nonswag.tnl.world.api.Environment;
import net.nonswag.tnl.world.api.generator.BuildWorldGenerator;
import net.nonswag.tnl.world.commands.WorldCommand;
import net.nonswag.tnl.world.listeners.WorldListener;
import net.nonswag.tnl.world.completer.WorldCommandTabCompleter;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Worlds extends JavaPlugin {

    private static Worlds instance;
    @Nonnull
    private final JsonConfig configuration = new JsonConfig("plugins/TNLWorlds/", "saves.json");

    public static Worlds getInstance() {
        return instance;
    }

    public static void setInstance(Worlds instance) {
        Worlds.instance = instance;
    }

    @Override
    public void onEnable() {
        setInstance(this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), getInstance());
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand("world", "tnl.world", new WorldCommand(), new WorldCommandTabCompleter());
        loadWorlds();
        if (Settings.AUTO_UPDATER.getValue()) {
            new PluginUpdate(getInstance()).downloadUpdate();
        }
    }

    public String getWorld(String name) {
        for(String s : getWorlds()) {
            String[] split = s.split("/");
            if (s.equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    public void loadWorlds() {
        for(String s : getWorlds()) {
            String[] split = s.split("/");
            if (Bukkit.getWorld(split[split.length - 1]) == null) {
                loadWorld(split[split.length - 1]);
            }
        }
    }

    public void loadWorld(String name) {
        if (Bukkit.getWorld(name) == null) {
            if (new File(name).exists()) {
                try {
                    String string = null;
                    if (getConfiguration().getJsonElement().getAsJsonObject().has(name + ".generator")) {
                        string = getConfiguration().getJsonElement().getAsJsonObject().get(name + ".generator").getAsString();
                    }
                    WorldType type = WorldType.getByName(getConfiguration().getJsonElement().getAsJsonObject().get(name + ".type").getAsString());
                    Environment environment = Environment.getByName(getConfiguration().getJsonElement().getAsJsonObject().get(name + ".environment").getAsString());
                    WorldCreator worldCreator = new WorldCreator(name);
                    if (string != null) {
                        Plugin plugin = Bukkit.getPluginManager().getPlugin(string);
                        if (plugin != null && plugin.isEnabled()) {
                            worldCreator.generator(plugin.getDefaultWorldGenerator(name, null));
                        }
                    }
                    worldCreator.createWorld();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new BuildWorldGenerator();
    }

    @Nonnull
    public List<String> getWorlds() {
        JsonArray worlds = getWorldArray();
        List<String> strings = new ArrayList<>();
        for (JsonElement world : worlds) {
            strings.add(world.getAsString());
        }
        return strings;
    }

    @Nonnull
    public JsonArray getWorldArray() {
        JsonElement jsonElement = getConfiguration().getJsonElement().getAsJsonObject().get("worlds");
        if (jsonElement != null && jsonElement.isJsonArray()) {
            return jsonElement.getAsJsonArray();
        } else {
            return new JsonArray();
        }
    }

    @Nonnull
    public JsonConfig getConfiguration() {
        return configuration;
    }
}
