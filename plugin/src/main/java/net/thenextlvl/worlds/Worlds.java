package net.thenextlvl.worlds;

import core.annotation.FieldsAreNonnullByDefault;
import core.annotation.ParametersAreNonnullByDefault;
import core.api.placeholder.Placeholder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.worlds.command.world.WorldCommand;
import net.thenextlvl.worlds.image.Image;
import net.thenextlvl.worlds.image.WorldImage;
import net.thenextlvl.worlds.listener.WorldListener;
import net.thenextlvl.worlds.util.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class Worlds extends JavaPlugin {
    @Accessors(fluent = true)
    private final Placeholder.Formatter<Audience> formatter = new Placeholder.Formatter<>();

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

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
    }

    private void registerCommands() {
        try {
            WorldCommand.register(this);
            // TODO: 06.05.23 /seed (World)
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
