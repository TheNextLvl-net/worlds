package net.thenextlvl.worlds.api.link;

import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum Relative implements Keyed {
    OVERWORLD(new NamespacedKey("relative", "overworld")),
    NETHER(new NamespacedKey("relative", "nether")),
    THE_END(new NamespacedKey("relative", "the_end"));

    private final NamespacedKey key;

    Relative(NamespacedKey key) {
        this.key = key;
    }

    @Override
    public NamespacedKey key() {
        return key;
    }
}
