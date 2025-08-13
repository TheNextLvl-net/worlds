package net.thenextlvl.worlds.api.generator;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/**
 * @since 3.0.0
 */
@ApiStatus.Experimental
public record LevelStem(DimensionType dimensionType) {
    public static final LevelStem OVERWORLD = new LevelStem(DimensionType.OVERWORLD);
    public static final LevelStem NETHER = new LevelStem(DimensionType.THE_NETHER);
    public static final LevelStem END = new LevelStem(DimensionType.THE_END);

    @Contract(pure = true)
    public static @Nullable LevelStem of(Key key) {
        if (key.equals(DimensionType.OVERWORLD.key())) return OVERWORLD;
        if (key.equals(DimensionType.THE_NETHER.key())) return NETHER;
        if (key.equals(DimensionType.THE_END.key())) return END;
        return null;
    }
}
