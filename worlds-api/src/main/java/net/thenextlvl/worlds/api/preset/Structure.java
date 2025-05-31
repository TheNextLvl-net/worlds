package net.thenextlvl.worlds.api.preset;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record Structure(Key key) implements Keyed {
    @Override
    public String toString() {
        return key().asString();
    }

    @Deprecated(forRemoval = true)
    public String structure() {
        return key().asString();
    }

    public Structure(@KeyPattern String string) {
        this(Key.key(string));
    }

    public static Structure literal(@KeyPattern String structure) {
        return new Structure(Key.key(structure));
    }

    @Deprecated(forRemoval = true)
    public static Structure minecraft(@KeyPattern.Value String structure) {
        return new Structure(Key.key(Key.MINECRAFT_NAMESPACE, structure));
    }
}
