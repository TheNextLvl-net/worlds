package net.thenextlvl.worlds.api.level;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.thenextlvl.worlds.api.generator.DimensionType;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

@NullMarked
public interface Level extends Keyed {
    Path getFile();

    Key getKey();

    String getName();

    GeneratorType getGeneratorType();

    DimensionType getDimensionType();

    Optional<Preset> getPreset();

    Optional<Generator> getGenerator();

    boolean isEnabled();

    boolean isWorldKnown();

    boolean isHardcore();

    boolean hasStructures();

    boolean hasBonusChest();

    long getSeed();

    Optional<World> create();

    interface Builder {
        Path directory();

        @Nullable
        Key key();

        Builder key(@Nullable Key key);

        @Nullable
        String name();

        Builder name(@Nullable String name);

        @Nullable
        DimensionType dimensionType();

        Builder dimensionType(@Nullable DimensionType type);

        @Nullable
        GeneratorType generatorType();

        Builder generatorType(@Nullable GeneratorType type);

        @Nullable
        Generator generator();

        Builder generator(@Nullable Generator generator);

        @Nullable
        Preset preset();

        Builder preset(@Nullable Preset preset);

        @Nullable
        Boolean enabled();

        Builder enabled(@Nullable Boolean enabled);

        @Nullable
        Boolean hardcore();

        Builder hardcore(@Nullable Boolean hardcore);

        @Nullable
        Boolean structures();

        Builder structures(@Nullable Boolean structures);

        @Nullable
        Boolean bonusChest();

        Builder bonusChest(@Nullable Boolean bonusChest);

        @Nullable
        Long seed();

        Builder seed(@Nullable Long seed);

        Level build();
    }
}
