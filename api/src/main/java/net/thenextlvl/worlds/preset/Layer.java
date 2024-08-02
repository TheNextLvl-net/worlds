package net.thenextlvl.worlds.preset;

import org.bukkit.Material;

public record Layer(String block, int height) {
    Layer(Material material, int height) {
        this(material.key().asString(), height);
    }
}
