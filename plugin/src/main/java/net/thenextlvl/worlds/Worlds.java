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
        saveDefaultPreset("presets/bottomless-pit.json");
        saveDefaultPreset("presets/desert.json");
        saveDefaultPreset("presets/overworld.json");
        saveDefaultPreset("presets/redstone-ready.json");
        saveDefaultPreset("presets/snowy-kingdom.json");
        saveDefaultPreset("presets/the-void.json");
        saveDefaultPreset("presets/water-world.json");
    }

    private void saveDefaultPreset(String preset) {
        if (!new File(getDataFolder(), preset).isFile())
            saveResource(preset, false);
    }
}
