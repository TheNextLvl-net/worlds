package net.thenextlvl.perworlds;

import core.i18n.file.ComponentBundle;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.thenextlvl.perworlds.group.PaperGroupProvider;
import net.thenextlvl.perworlds.listener.WorldListener;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.io.File;
import java.util.Locale;
import java.util.Set;

public class SharedWorlds {
    public static final String ISSUES = "https://github.com/TheNextLvl-net/worlds/issues/new?template=bug_report.yml";
    private final PaperGroupProvider groupProvider;
    private final Metrics metrics;
    private final Plugin plugin;
    private final ComponentBundle bundle;
    private final File dataFolder = new File("plugins", "PerWorlds");

    public SharedWorlds(Plugin plugin) {
        this.plugin = plugin;
        this.metrics = new Metrics(plugin, 25295);
        this.bundle = new ComponentBundle(new File(dataFolder, "translations"), audience ->
                audience instanceof Player player ? player.locale() : Locale.US)
                .register("per-worlds", Locale.US)
                .register("per-worlds_german", Locale.GERMANY)
                .miniMessage(bundle -> MiniMessage.builder().tags(TagResolver.resolver(
                        TagResolver.standard(),
                        Placeholder.component("prefix", bundle.component(Locale.US, "prefix"))
                )).build());
        this.groupProvider = new PaperGroupProvider(this);
    }

    public void onLoad() {
        registerServices();
        loadGroups();
    }

    private void loadGroups() {
        var suffix = ".json";
        var files = groupProvider.getDataFolder().listFiles((file, name) -> name.endsWith(suffix));
        if (files != null) for (var file : files) {
            var name = file.getName();
            name = name.substring(0, name.length() - suffix.length());
            groupProvider.createGroup(name);
        }
    }

    public void onEnable() {
        registerListeners();
        addCustomCharts();
    }

    public void onDisable() {
        groupProvider.getGroups().forEach(group -> {
            group.persistPlayerData();
            group.persist();
        });
        metrics.shutdown();
    }

    public Server getServer() {
        return plugin.getServer();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public GroupProvider groupProvider() {
        return groupProvider;
    }

    public ComponentBundle bundle() {
        return bundle;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new WorldListener(groupProvider), plugin);
    }

    private void registerServices() {
        plugin.getServer().getServicesManager().register(GroupProvider.class, groupProvider, plugin, ServicePriority.Highest);
    }

    private static final Set<String> knownWorldManagers = Set.of( // list ordered by likelihood of a plugin being used
            "Worlds",
            "Multiverse-Core", // https://github.com/Multiverse/Multiverse-Core/
            "MultiWorld", // https://dev.bukkit.org/projects/multiworld-v-2-0 // https://modrinth.com/plugin/multiworld-bukkit
            "PhantomWorlds", // https://github.com/TheNewEconomy/PhantomWorlds
            "Hyperverse", // https://github.com/Incendo/Hyperverse
            "LightWorlds", // https://github.com/justin0-0/LightWorlds
            "SolarSystem", // https://github.com/OneLiteFeatherNET/SolarSystemPlugin
            "MoreFoWorld", // https://github.com/Folia-Inquisitors/MoreFoWorld
            "WorldManager", // https://www.spigotmc.org/resources/worldmanager-1-8-1-18-free-download-api.101875/
            "WorldMaster", // https://www.spigotmc.org/resources/worldmaster.101171/
            "TheGalaxyLimits", // https://hangar.papermc.io/TheGlitchedVirus/thegalaxylimits
            "BulMultiverse", // https://github.com/BulPlugins/BulMultiverse
            "worldmgr" // https://dev.bukkit.org/projects/worldmgr
    );

    private void addCustomCharts() {
        metrics.addCustomChart(new SimplePie("world_management_plugin", () -> knownWorldManagers.stream()
                .filter(name -> plugin.getServer().getPluginManager().getPlugin(name) != null)
                .findAny().orElse("None")));
    }
}
