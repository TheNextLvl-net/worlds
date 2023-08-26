package net.thenextlvl.worlds;

import core.annotation.FieldsAreNonnullByDefault;
import core.annotation.ParametersAreNonnullByDefault;
import core.api.placeholder.Placeholder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.thenextlvl.worlds.command.link.LinkCommand;
import net.thenextlvl.worlds.command.world.WorldCommand;
import net.thenextlvl.worlds.image.Image;
import net.thenextlvl.worlds.image.WorldImage;
import net.thenextlvl.worlds.link.LinkFile;
import net.thenextlvl.worlds.listener.WorldListener;
import net.thenextlvl.worlds.util.Messages;
import net.thenextlvl.worlds.util.Placeholders;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class Worlds extends JavaPlugin {
    private final Placeholder.Formatter<Audience> formatter = new Placeholder.Formatter<>();
    private final LinkFile linkFile = new LinkFile(new File(Bukkit.getWorldContainer(), ".links"));
    private final Metrics metrics = new Metrics(this, 19652);

    @Override
    public void onLoad() {
        Placeholders.init(this);
    }

    @Override
    public void onEnable() {
        Image.findImages().stream()
                .filter(WorldImage::loadOnStart)
                .forEach(Image::load);
        registerListeners();
        registerCommands();
        saveDefaultPresets();
    }

    @Override
    public void onDisable() {
        Bukkit.getWorlds().stream()
                .map(Image::get)
                .filter(Objects::nonNull)
                .forEach(image -> {
                    var worldImage = image.getWorldImage();
                    if (worldImage.deletion() == null) return;
                    image.getWorld().getPlayers().forEach(player -> player.kick(MiniMessage.miniMessage()
                            .deserialize(Messages.KICK_WORLD_DELETED.message(player.locale(), player))));
                    image.delete(worldImage.deletion().keepImage(), true);
                });
        metrics.shutdown();
        linkFile().save();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new WorldListener(this), this);
    }

    private void registerCommands() {
        try {
            LinkCommand.register(this);
            WorldCommand.register(this);
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
