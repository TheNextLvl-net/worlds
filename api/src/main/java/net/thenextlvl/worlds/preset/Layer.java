package net.thenextlvl.worlds.preset;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.jetbrains.annotations.Range;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class Layer {
    private Material block;
    private @Range(from = 1, to = Long.MAX_VALUE) int height;
}
