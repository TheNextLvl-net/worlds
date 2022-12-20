package net.nonswag.tnl.world.api.world;

import lombok.Getter;
import net.nonswag.core.api.annotation.FieldsAreNonnullByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum WorldType {
    NORMAL(org.bukkit.WorldType.NORMAL, "normal"),
    FLAT(org.bukkit.WorldType.FLAT, "flat"),
    AMPLIFIED(org.bukkit.WorldType.AMPLIFIED, "amplified"),
    LARGE_BIOMES(org.bukkit.WorldType.LARGE_BIOMES, "large-biomes");

    private final org.bukkit.WorldType worldType;
    private final String name;

    WorldType(org.bukkit.WorldType worldType, String name) {
        this.worldType = worldType;
        this.name = name;
    }

    @Nullable
    public static WorldType getByName(String name) {
        for (WorldType type : values()) {
            if (type.getName().equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) return type;
        }
        return null;
    }

    public static WorldType valueOf(@Nullable org.bukkit.WorldType bukkit) {
        if (bukkit == null) return NORMAL;
        for (WorldType type : values()) if (type.getWorldType().equals(bukkit)) return type;
        return NORMAL;
    }
}
