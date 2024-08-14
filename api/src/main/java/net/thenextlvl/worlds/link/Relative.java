package net.thenextlvl.worlds.link;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
public enum Relative implements Keyed {
    OVERWORLD(new NamespacedKey("relative", "overworld")),
    NETHER(new NamespacedKey("relative", "nether")),
    THE_END(new NamespacedKey("relative", "the_end"));

    private final NamespacedKey key;

    public static Optional<Relative> valueOf(Key key) {
        return Arrays.stream(values())
                .filter(value -> value.key().equals(key))
                .findAny();
    }

    public static Optional<Relative> valueOf(World.Environment environment) {
        return Optional.ofNullable(switch (environment) {
            case NORMAL -> Relative.OVERWORLD;
            case NETHER -> Relative.NETHER;
            case THE_END -> Relative.THE_END;
            default -> null;
        });
    }
}
