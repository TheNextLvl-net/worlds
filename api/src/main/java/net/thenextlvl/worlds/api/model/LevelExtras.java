package net.thenextlvl.worlds.api.model;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

public record LevelExtras(
        @Nullable NamespacedKey key,
        @Nullable Generator generator,
        boolean enabled
) {
}
