package net.thenextlvl.perworlds;

import core.i18n.file.ComponentBundle;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.thenextlvl.perworlds.group.PaperGroupProvider;
import net.thenextlvl.perworlds.listener.ChatListener;
import net.thenextlvl.perworlds.listener.ConnectionListener;
import net.thenextlvl.perworlds.listener.MessageListener;
import net.thenextlvl.perworlds.listener.RespawnListener;
import net.thenextlvl.perworlds.listener.TeleportListener;
import net.thenextlvl.perworlds.listener.WorldListener;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

@NullMarked
public class SharedWorlds {
    public static final String ISSUES = "https://github.com/TheNextLvl-net/worlds/issues/new?template=bug_report.yml";
    private final ComponentLogger logger = ComponentLogger.logger("PerWorlds");
    private final PaperGroupProvider provider;
    private final Plugin plugin;
    private final ComponentBundle bundle;
    private final Path dataPath = Path.of("plugins", "PerWorlds");

    public SharedWorlds(Plugin plugin) {
        this.plugin = plugin;
        var key = Key.key("perworlds", "translations");
        var translations = dataPath.resolve("translations");
        this.bundle = ComponentBundle.builder(key, translations)
                .placeholder("prefix", "prefix")
                .resource("per-worlds.properties", Locale.US)
                .resource("per-worlds_german.properties", Locale.GERMANY)
                .build();
        this.provider = new PaperGroupProvider(this);
    }

    public void onLoad() {
        provider.getLogger().warn("PerWorlds is still in early beta");
        provider.getLogger().warn("Although mostly stable, please be sure to always use the latest version");
        provider.getLogger().warn("We suggest taking backups of your player data before using this plugin");
        provider.getLogger().warn("Please report any issues you encounter to {}", ISSUES);
        registerServices();
        loadGroups();
    }

    private void loadGroups() {
        var suffix = ".dat";
        var files = provider.getDataFolder().listFiles((file, name) -> name.endsWith(suffix));
        if (files != null) for (var file : files) {
            var name = file.getName();
            name = name.substring(0, name.length() - suffix.length());
            if (!provider.hasGroup(name)) provider.createGroup(name);
        }
    }

    public void onEnable() {
        registerListeners();
    }

    public void onDisable() {
        var groups = new ArrayList<>(provider.getGroups());
        groups.add(provider.getUnownedWorldGroup());
        groups.forEach(group -> {
            group.persistPlayerData();
            group.persist();
        });
    }

    public ComponentLogger getLogger() {
        return logger;
    }

    public Server getServer() {
        return plugin.getServer();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public GroupProvider groupProvider() {
        return provider;
    }

    public ComponentBundle bundle() {
        return bundle;
    }

    public File getDataFolder() {
        return dataPath.toFile();
    }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new ChatListener(provider), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ConnectionListener(provider), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MessageListener(provider), plugin);
        plugin.getServer().getPluginManager().registerEvents(new RespawnListener(provider), plugin);
        plugin.getServer().getPluginManager().registerEvents(new TeleportListener(provider), plugin);
        plugin.getServer().getPluginManager().registerEvents(new WorldListener(provider), plugin);
    }

    private void registerServices() {
        plugin.getServer().getServicesManager().register(GroupProvider.class, provider, plugin, ServicePriority.Highest);
    }
}
