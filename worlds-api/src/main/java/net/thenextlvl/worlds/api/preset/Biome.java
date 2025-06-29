package net.thenextlvl.worlds.api.preset;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record Biome(Key key) implements Keyed {
    public Biome(org.bukkit.block.Biome biome) {
        this(biome.key());
    }

    @Override
    public String toString() {
        return key().asString();
    }

    public static Biome literal(@KeyPattern String string) {
        return new Biome(Key.key(string));
    }
}
