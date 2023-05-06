package net.thenextlvl.worlds;

import core.annotation.FieldsAreNonnullByDefault;
import core.annotation.ParametersAreNonnullByDefault;
import core.api.placeholder.Placeholder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.worlds.command.world.WorldCommand;
import net.thenextlvl.worlds.generator.BuildersDreamGenerator;
import net.thenextlvl.worlds.generator.VoidGenerator;
import net.thenextlvl.worlds.util.Placeholders;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

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
        registerCommands();
    }

    private void registerCommands() {
        try {
            WorldCommand.register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, @Nullable String id) {
        if (VoidGenerator.NAME.equalsIgnoreCase(id))
            return VoidGenerator.getWorldGenerator(worldName);
        if (BuildersDreamGenerator.NAME.equalsIgnoreCase(id))
            return BuildersDreamGenerator.getWorldGenerator(worldName);
        return null;
    }

    @Nullable
    @Override
    public BiomeProvider getDefaultBiomeProvider(String worldName, @Nullable String id) {
        if (VoidGenerator.NAME.equalsIgnoreCase(id))
            return VoidGenerator.getBiomeProvider(worldName);
        if (BuildersDreamGenerator.NAME.equalsIgnoreCase(id))
            return BuildersDreamGenerator.getBiomeProvider(worldName);
        return null;
    }
}
