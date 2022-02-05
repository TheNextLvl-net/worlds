package net.nonswag.tnl.world.generators;

import net.nonswag.tnl.listener.api.plugin.PluginBuilder;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CustomGenerator extends PluginBuilder {

    @Nonnull
    private static final List<Plugin> additionalGenerators = new ArrayList<>();

    protected CustomGenerator(@Nonnull String name) {
        super(name);
    }

    @Nonnull
    @Override
    public CustomGenerator register() {
        if (!isRegistered()) additionalGenerators.add(this);
        return this;
    }

    @Override
    public void unregister() {
        additionalGenerators.remove(this);
    }

    @Override
    public boolean isRegistered() {
        return additionalGenerators.contains(this);
    }

    @Nonnull
    public static List<Plugin> getAdditionalGenerators() {
        return new ArrayList<>(additionalGenerators);
    }

    @Override
    public abstract ChunkGenerator getDefaultWorldGenerator(@Nonnull String name, @Nullable String id);
}
