package net.thenextlvl.worlds.model;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

public record LevelExtras(
        @Nullable NamespacedKey key,
        boolean enabled
) {
}
