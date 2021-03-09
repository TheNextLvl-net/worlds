package net.nonswag.tnl.world.api;

import javax.annotation.Nonnull;

public enum WorldType {
    NORMAL(org.bukkit.WorldType.NORMAL),
    FLAT(org.bukkit.WorldType.FLAT),
    VERSION_1_1(org.bukkit.WorldType.VERSION_1_1),
    LARGE_BIOMES(org.bukkit.WorldType.LARGE_BIOMES),
    AMPLIFIED(org.bukkit.WorldType.AMPLIFIED),
    CUSTOMIZED(org.bukkit.WorldType.CUSTOMIZED),
    BUFFET(org.bukkit.WorldType.BUFFET);

    @Nonnull
    private final org.bukkit.WorldType worldType;

    WorldType(@Nonnull org.bukkit.WorldType worldType) {
        this.worldType = worldType;
    }

    @Nonnull
    public org.bukkit.WorldType getWorldType() {
        return worldType;
    }
}
