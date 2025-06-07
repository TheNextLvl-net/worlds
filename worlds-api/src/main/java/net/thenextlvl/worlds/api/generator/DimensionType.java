package net.thenextlvl.worlds.api.generator;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

// https://minecraft.wiki/w/Dimension_type
public record DimensionType(Key key) implements Keyed {
    public static final DimensionType OVERWORLD = new DimensionType(Key.key("overworld"));

    public static final DimensionType OVERWORLD_CAVES = new DimensionType(Key.key("overworld_caves"));

    public static final DimensionType THE_END = new DimensionType(Key.key("the_end"));

    public static final DimensionType THE_NETHER = new DimensionType(Key.key("the_nether"));
}
