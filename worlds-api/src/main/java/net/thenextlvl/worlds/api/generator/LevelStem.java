package net.thenextlvl.worlds.api.generator;

public record LevelStem(DimensionType dimensionType, GeneratorType generatorType) {
    public static final LevelStem OVERWORLD = new LevelStem(DimensionType.OVERWORLD, GeneratorType.NORMAL);
    public static final LevelStem NETHER = new LevelStem(DimensionType.THE_NETHER, GeneratorType.NORMAL);
    public static final LevelStem END = new LevelStem(DimensionType.THE_END, GeneratorType.NORMAL);
}
