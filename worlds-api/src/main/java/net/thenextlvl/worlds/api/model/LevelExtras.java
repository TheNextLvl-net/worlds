package net.thenextlvl.worlds.api.model;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record LevelExtras(
        @Nullable NamespacedKey key,
        @Nullable Generator generator,
        boolean enabled
) {
}
