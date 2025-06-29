package net.thenextlvl.worlds.api.generator;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record LevelStem(DimensionType dimensionType) {
    public static final LevelStem OVERWORLD = new LevelStem(DimensionType.OVERWORLD);
    public static final LevelStem NETHER = new LevelStem(DimensionType.THE_NETHER);
    public static final LevelStem END = new LevelStem(DimensionType.THE_END);
}
