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
import net.thenextlvl.worlds.image.CraftImageProvider;
import net.thenextlvl.worlds.image.WorldImage;
import net.thenextlvl.worlds.link.CraftLinkRegistry;
import net.thenextlvl.worlds.link.LinkRegistry;
import net.thenextlvl.worlds.listener.PortalListener;
import net.thenextlvl.worlds.listener.WorldListener;
import net.thenextlvl.worlds.preset.Presets;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
@FieldsAreNotNullByDefault
@ParametersAreNotNullByDefault
public class Worlds extends JavaPlugin {
    private final CraftImageProvider imageProvider = new CraftImageProvider(this);
    private final CraftLinkRegistry linkRegistry = new CraftLinkRegistry(this);

    private final File presetsFolder = new File(getDataFolder(), "presets");
    private final File translations = new File(getDataFolder(), "translations");

    private final ComponentBundle bundle = new ComponentBundle(translations, audience ->
            audience instanceof Player player ? player.locale() : Locale.US)
            .register("worlds", Locale.US)
            .register("worlds_german", Locale.GERMANY)
            .fallback(Locale.US);

    private final Metrics metrics = new Metrics(this, 19652);

    @Override
    public void onLoad() {
        bundle().miniMessage(MiniMessage.builder().tags(TagResolver.resolver(
                TagResolver.standard(),
                Placeholder.component("prefix", bundle().component(Locale.US, "prefix"))
        )).build());

        Bukkit.getServicesManager().register(LinkRegistry.class, linkRegistry(), this, ServicePriority.Highest);

        saveDefaultPresets();
    }

    @Override
    public void onEnable() {
        imageProvider().findImages().stream()
                .filter(WorldImage::loadOnStart)
                .forEach(imageProvider()::load);
        linkRegistry().loadLinks();
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        Bukkit.getWorlds().stream()
                .map(imageProvider()::get)
                .filter(Objects::nonNull)
                .forEach(image -> {
                    var deletionType = image.getWorldImage().deletionType();
                    if (deletionType != null) {
                        image.getWorld().getPlayers().forEach(player -> player.kick(Bukkit.shutdownMessage()));
                        image.deleteNow(deletionType.keepImage(), false);
                    } else if (!image.getWorldImage().autoSave()) {
                        image.getWorld().getPlayers().forEach(player -> player.kick(Bukkit.shutdownMessage()));
                        image.unload();
                    }
                });
        linkRegistry().saveLinks();
        metrics().shutdown();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PortalListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(this), this);
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
