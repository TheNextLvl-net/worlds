package net.thenextlvl.worlds.api.link;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Optional;

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

    public static Optional<Relative> valueOf(Key key) {
        return Arrays.stream(values())
                .filter(value -> value.key().equals(key))
                .findAny();
    }
}
