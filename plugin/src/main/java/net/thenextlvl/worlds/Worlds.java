package net.thenextlvl.worlds;

import core.annotation.FieldsAreNonnullByDefault;
import core.api.placeholder.Placeholder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import org.bukkit.plugin.java.JavaPlugin;
import worlds.command.world.WorldCommand;
import worlds.generator.BuildersDreamGenerator;
import worlds.generator.VoidGenerator;
import worlds.util.Placeholders;

@Getter
@FieldsAreNonnullByDefault
public class Worlds extends JavaPlugin {
    @Accessors(fluent = true)
    private final Placeholder.Formatter<Audience> formatter = new Placeholder.Formatter<>();

    @Override
    public void onLoad() {
        Placeholders.init(this);
    }

    @Override
    public void onEnable() {
        VoidGenerator.getInstance().register();
        BuildersDreamGenerator.getInstance().register();
        registerCommands();
    }

    private void registerCommands() {
        try {
            WorldCommand.register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}