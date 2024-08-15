package net.thenextlvl.worlds;

import core.annotation.FieldsAreNotNullByDefault;
import core.annotation.ParametersAreNotNullByDefault;
import core.i18n.file.ComponentBundle;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.thenextlvl.worlds.command.WorldCommand;
import net.thenextlvl.worlds.controller.WorldLinkController;
import net.thenextlvl.worlds.view.GeneratorView;
import net.thenextlvl.worlds.link.LinkController;
import net.thenextlvl.worlds.listener.PortalListener;
import net.thenextlvl.worlds.listener.ServerListener;
import net.thenextlvl.worlds.preset.Presets;
import net.thenextlvl.worlds.version.PluginVersionChecker;
import net.thenextlvl.worlds.view.LevelView;
import net.thenextlvl.worlds.view.PaperLevelView;
import net.thenextlvl.worlds.view.PluginGeneratorView;
import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;

import static org.bukkit.persistence.PersistentDataType.BOOLEAN;
import static org.bukkit.persistence.PersistentDataType.STRING;

@Getter
@Accessors(fluent = true)
@FieldsAreNotNullByDefault
@ParametersAreNotNullByDefault
public class WorldsPlugin extends JavaPlugin {
    private final GeneratorView generatorView = new PluginGeneratorView();
    private final LevelView levelView = new PaperLevelView(this);

    private final LinkController linkController = new WorldLinkController(this);

    private final File presetsFolder = new File(getDataFolder(), "presets");
    private final File translations = new File(getDataFolder(), "translations");

    private final ComponentBundle bundle = new ComponentBundle(translations, audience ->
            audience instanceof Player player ? player.locale() : Locale.US)
            .register("worlds", Locale.US)
            .register("worlds_german", Locale.GERMANY)
            .miniMessage(bundle -> MiniMessage.builder().tags(TagResolver.resolver(
                    TagResolver.standard(),
                    Placeholder.component("prefix", bundle.component(Locale.US, "prefix"))
            )).build());

    private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
    private final Metrics metrics = new Metrics(this, 19652);

    @Override
    public void onLoad() {
        if (presetsFolder().list() == null) saveDefaultPresets();
        versionChecker().checkVersion();
    }

    @Override
    public void onEnable() {
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        metrics().shutdown();
        unloadWorlds();
    }

    private void unloadWorlds() {
        getServer().getWorlds().stream().filter(world -> !world.isAutoSave()).forEach(world -> {
            world.getPlayers().forEach(player -> player.kick(getServer().shutdownMessage()));
            getServer().unloadWorld(world, false);
        });
    }

    public void persistWorld(World world, boolean enabled) {
        if (world.key().asString().equals("minecraft:overworld")) return;
        var container = world.getPersistentDataContainer();
        container.set(new NamespacedKey("worlds", "world_key"), STRING, world.getKey().asString());
        container.set(new NamespacedKey("worlds", "enabled"), BOOLEAN, enabled);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
    }

    private void registerCommands() {
        new WorldCommand(this).register();
    }

    private void saveDefaultPresets() {
        Presets.BOTTOMLESS_PIT.saveToFile(new File(presetsFolder(), "bottomless-pit.json"), true);
        Presets.CLASSIC_FLAT.saveToFile(new File(presetsFolder(), "classic-flat.json"), true);
        Presets.DESERT.saveToFile(new File(presetsFolder(), "desert.json"), true);
        Presets.OVERWORLD.saveToFile(new File(presetsFolder(), "overworld.json"), true);
        Presets.REDSTONE_READY.saveToFile(new File(presetsFolder(), "redstone-ready.json"), true);
        Presets.SNOWY_KINGDOM.saveToFile(new File(presetsFolder(), "snowy-kingdom.json"), true);
        Presets.THE_VOID.saveToFile(new File(presetsFolder(), "the-void.json"), true);
        Presets.TUNNELERS_DREAM.saveToFile(new File(presetsFolder(), "tunnelers-dream.json"), true);
        Presets.WATER_WORLD.saveToFile(new File(presetsFolder(), "water-world.json"), true);
    }
}
