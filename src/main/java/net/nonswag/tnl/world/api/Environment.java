package net.nonswag.tnl.world.api;

import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum Environment {
    NORMAL(World.Environment.NORMAL, "normal"),
    NETHER(World.Environment.NETHER, "nether"),
    THE_END(World.Environment.THE_END, "end");

    @Nonnull
    private final World.Environment environment;
    @Nonnull
    private final String name;

    Environment(@Nonnull World.Environment environment, @Nonnull String name) {
        this.environment = environment;
        this.name = name;
    }

    @Nonnull
    public World.Environment getEnvironment() {
        return environment;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nullable
    public static Environment getByName(@Nonnull String name) {
        for (Environment environment : values()) if (environment.getName().equalsIgnoreCase(name)) return environment;
        return null;
    }
}
