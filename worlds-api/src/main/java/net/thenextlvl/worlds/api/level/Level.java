package net.thenextlvl.worlds.api.level;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

@NullMarked
public interface Level extends Keyed {
    Path getFile();

    String getName();

    GeneratorType getGeneratorType();

    LevelStem getLevelStem();

    Optional<Preset> getPreset();

    Optional<Generator> getGenerator();

    TriState isKeepSpawnLoaded();

    TriState isEnabled();

    boolean isWorldKnown();

    boolean isHardcore();

    boolean hasStructures();

    boolean hasBonusChest();

    long getSeed();

    Builder toBuilder();

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
        LevelStem levelStem();

        Builder levelStem(@Nullable LevelStem type);

        @Nullable
        GeneratorType generatorType();

        Builder generatorType(@Nullable GeneratorType type);

        @Nullable
        Generator generator();

        Builder generator(@Nullable Generator generator);

        @Nullable
        Preset preset();

        Builder preset(@Nullable Preset preset);

        TriState keepSpawnLoaded();

        Builder keepSpawnLoaded(TriState keepSpawnLoaded);

        TriState enabled();

        Builder enabled(TriState enabled);

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
        Boolean worldKnown();

        Builder worldKnown(@Nullable Boolean worldKnown);

        @Nullable
        Long seed();

        Builder seed(@Nullable Long seed);

        /**
         * Constructs a new Level instance based on the current state of the builder.
         *
         * @return the constructed Level instance
         */
        Level build();
    }
}
