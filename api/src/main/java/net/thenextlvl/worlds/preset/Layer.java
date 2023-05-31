package net.thenextlvl.worlds.preset;

import org.bukkit.Material;
import org.jetbrains.annotations.Range;

public record Layer(Material block, @Range(from = 1, to = Long.MAX_VALUE) int height) {
}
