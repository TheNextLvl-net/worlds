package net.nonswag.tnl.world.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum WorldType {
    NORMAL(org.bukkit.WorldType.NORMAL, "normal"),
    FLAT(org.bukkit.WorldType.FLAT, "flat");

    @Nonnull
    private final org.bukkit.WorldType worldType;
    @Nonnull
    private final String name;

    WorldType(@Nonnull org.bukkit.WorldType worldType, @Nonnull String name) {
        this.worldType = worldType;
        this.name = name;
    }

    @Nonnull
    public org.bukkit.WorldType getWorldType() {
        return worldType;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nullable
    public static WorldType getByName(@Nonnull String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (WorldType type : values()) if (type.getName().equalsIgnoreCase(name)) return type;
            return null;
        }
    }
}
