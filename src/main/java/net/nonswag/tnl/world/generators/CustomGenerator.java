package net.nonswag.tnl.world.generators;

import net.nonswag.tnl.listener.api.plugin.PluginBuilder;
import net.nonswag.tnl.world.Worlds;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CustomGenerator extends PluginBuilder {

    @Nonnull
    private static final List<CustomGenerator> additionalGenerators = new ArrayList<>();

    protected CustomGenerator(@Nonnull String name) {
        super(name, Worlds.getInstance());
        this.enabled = true;
    }

    @Nonnull
    @Override
    public CustomGenerator register() {
        if (!isRegistered()) additionalGenerators.add(this);
        return this;
    }

    @Nonnull
    @Override
    public CustomGenerator unregister() {
        additionalGenerators.remove(this);
        return this;
    }

    @Override
    public boolean isRegistered() {
        return additionalGenerators.contains(this);
    }

    @Nullable
    @Override
    public ChunkGenerator getDefaultWorldGenerator(@Nonnull String name, @Nullable String id) {
        return null;
    }

    @Nullable
    public WorldCreator getWorldCreator(@Nonnull String name) {
        return null;
    }

    @Nonnull
    public static List<CustomGenerator> getAdditionalGenerators() {
        return new ArrayList<>(additionalGenerators);
    }

    @Nullable
    public static CustomGenerator getGenerator(@Nonnull String name) {
        for (CustomGenerator g : additionalGenerators) if (g.getName().equals(name)) return g;
        for (CustomGenerator g : additionalGenerators) if (g.getName().equalsIgnoreCase(name)) return g;
        return null;
    }
}
