package net.thenextlvl.worlds.preset;

import com.google.common.base.Preconditions;

public record Biome(String provider, String biome) {

    public static Biome minecraft(String biome) {
        return new Biome("minecraft", biome);
    }

    public static Biome bukkit(org.bukkit.block.Biome biome) {
        return new Biome(biome.getKey().getNamespace(), biome.getKey().getKey());
    }

    public static Biome literal(String string) {
        var split = string.split(":", 2);
        Preconditions.checkArgument(split.length == 2, "Not a valid biome: " + string);
        return new Biome(split[0], split[1]);
    }

    @Override
    public String toString() {
        return provider() + ":" + biome();
    }
}
