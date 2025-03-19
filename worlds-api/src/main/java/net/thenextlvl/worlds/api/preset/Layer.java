package net.thenextlvl.worlds.api.preset;

import org.bukkit.Material;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record Layer(String block, int height) {
    Layer(Material material, int height) {
        this(material.key().asString(), height);
    }

    @Override
    public String toString() {
        return height() != 1 ? height() + "*" + block() : block();
    }
}
