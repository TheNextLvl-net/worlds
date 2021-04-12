package net.nonswag.tnl.world.api;

import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum Environment {
    NORMAL(World.Environment.NORMAL),
    NETHER(World.Environment.NETHER),
    THE_END(World.Environment.THE_END);

    @Nonnull private final World.Environment environment;

    Environment(@Nonnull World.Environment environment) {
        this.environment = environment;
    }

    @Nonnull
    public World.Environment getEnvironment() {
        return environment;
    }

    @Override
    public String toString() {
        return "Environment{" +
                "environment=" + environment +
                '}';
    }

    @Nullable
    public static Environment getByName(@Nonnull String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
