package net.thenextlvl.worlds;

import core.annotation.FieldsAreNotNullByDefault;
import core.annotation.ParametersAreNotNullByDefault;
import core.api.file.format.GsonFile;
import core.i18n.file.ComponentBundle;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.thenextlvl.worlds.command.config.Config;
import net.thenextlvl.worlds.command.link.LinkCommand;
import net.thenextlvl.worlds.command.world.WorldCommand;
import net.thenextlvl.worlds.image.Image;
import net.thenextlvl.worlds.image.WorldImage;
import net.thenextlvl.worlds.link.LinkFile;
import net.thenextlvl.worlds.listener.WorldListener;
import net.thenextlvl.worlds.preset.Presets;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
@FieldsAreNotNullByDefault
@ParametersAreNotNullByDefault
public class Worlds extends JavaPlugin {
    private final LinkFile linkFile = new LinkFile(new File(Bukkit.getWorldContainer(), ".links"));
    private final File presetsFolder = new File(getDataFolder(), "presets");
    private final Metrics metrics = new Metrics(this, 19652);
    private final File translations = new File(getDataFolder(), "translations");
    private final ComponentBundle bundle = new ComponentBundle(translations, audience ->
            audience instanceof Player player ? player.locale() : Locale.US)
            .register("worlds", Locale.US)
            .register("worlds_german", Locale.GERMANY)
            .fallback(Locale.US);
    private final GsonFile<Config> configFile = new GsonFile<>(
            new File(getDataFolder(), "config.json"),
            new Config()
    ).saveIfAbsent();

    @Override
    public void onLoad() {
        saveDefaultPresets();
        bundle().miniMessage(MiniMessage.builder().tags(TagResolver.resolver(
                TagResolver.standard(),
                Placeholder.parsed("prefix", bundle().format(Locale.US, "prefix"))
        )).build());
    }

    @Override
    public void onEnable() {
        Image.findImages().stream()
                .filter(WorldImage::loadOnStart)
                .forEach(Image::load);
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        Bukkit.getWorlds().stream()
                .map(Image::get)
                .filter(Objects::nonNull)
                .forEach(image -> {
                    var worldImage = image.getWorldImage();
                    if (worldImage.deletion() != null) {
                        image.getWorld().getPlayers().forEach(player -> player.kick(
                                Bukkit.shutdownMessage(),
                                PlayerKickEvent.Cause.RESTART_COMMAND
                        ));
                        image.deleteImmediately(worldImage.deletion().keepImage(), false);
                    } else if (!worldImage.autoSave()) {
                        image.getWorld().getPlayers().forEach(player -> player.kick(
                                Bukkit.shutdownMessage(),
                                PlayerKickEvent.Cause.RESTART_COMMAND
                        ));
                        image.unload();
                    }
                });
        metrics.shutdown();
        linkFile().save();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new WorldListener(this), this);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void registerCommands() {
        try {
            LinkCommand.register(this);
            WorldCommand.register();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
