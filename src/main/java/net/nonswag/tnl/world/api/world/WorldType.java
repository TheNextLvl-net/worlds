package net.nonswag.tnl.world.api.world;

import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Getter
public enum WorldType {
    NORMAL(org.bukkit.WorldType.NORMAL, "normal"),
    FLAT(org.bukkit.WorldType.FLAT, "flat"),
    AMPLIFIED(org.bukkit.WorldType.AMPLIFIED, "amplified"),
    LARGE_BIOMES(org.bukkit.WorldType.LARGE_BIOMES, "large-biomes");

    @Nonnull
    private final org.bukkit.WorldType worldType;
    @Nonnull
    private final String name;

    WorldType(@Nonnull org.bukkit.WorldType worldType, @Nonnull String name) {
        this.worldType = worldType;
        this.name = name;
    }

    @Nullable
    public static WorldType getByName(@Nonnull String name) {
        for (WorldType type : values()) {
            if (type.getName().equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) return type;
        }
        return null;
    }

    @Nonnull
    public static WorldType valueOf(@Nullable org.bukkit.WorldType bukkit) {
        if (bukkit == null) return NORMAL;
        for (WorldType type : values()) if (type.getWorldType().equals(bukkit)) return type;
        return NORMAL;
    }
}
