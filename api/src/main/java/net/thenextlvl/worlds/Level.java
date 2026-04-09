package net.thenextlvl.worlds;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.worlds.experimental.GeneratorType;
import net.thenextlvl.worlds.generator.Generator;
import net.thenextlvl.worlds.preset.Preset;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface Level extends Keyed {
    @Contract(pure = true)
    Path getDirectory();

    @Contract(pure = true)
    String getName();

    @Contract(pure = true)
    long getSeed();

    @Contract(pure = true)
    boolean isHardcore();

    @Contract(pure = true)
    boolean hasStructures();

    @Contract(pure = true)
    boolean hasBonusChest();

    @Contract(pure = true)
    GeneratorType getGeneratorType();

    @Contract(pure = true)
    Optional<Generator> getGenerator();

    @Contract(pure = true)
    Optional<ChunkGenerator> getChunkGenerator();

    @Contract(pure = true)
    Optional<BiomeProvider> getBiomeProvider();

    @Contract(pure = true)
    Optional<Preset> getPreset();

    @Unmodifiable
    @Contract(pure = true)
    Set<World> getDimensions();

    // todo: Stuff that has to go

    @ApiStatus.Experimental
    @Contract(pure = true)
    LevelStem getLevelStem();

    @ApiStatus.Experimental
    TriState isEnabled();

    @ApiStatus.Experimental
    boolean ignoreLevelData();

    @ApiStatus.Experimental
    TriState initialized();

    // todo end

    @Unmodifiable
    @Contract(pure = true)
    Set<Key> getDimensionKeys();

    @Contract(pure = true)
    Optional<World> getDimension(Key key);

    @Contract(mutates = "this")
    CompletableFuture<World> createDimension(Key key, LevelStem stem);

    @Contract(mutates = "this")
    CompletableFuture<World> loadDimension(Key key);

    @Contract(pure = true)
    Builder toBuilder();

    interface Builder {

        @Contract(pure = true)
        Path directory();

        @Contract(mutates = "this")
        Builder directory(Path directory);

        @Nullable
        @Contract(pure = true)
        String name();

        @Contract(mutates = "this")
        Builder name(@Nullable String name);

        @Nullable
        @Contract(pure = true)
        Long seed();

        @Contract(mutates = "this")
        Builder seed(@Nullable Long seed);

        @Nullable
        @Contract(pure = true)
        Boolean hardcore();

        @Contract(mutates = "this")
        Builder hardcore(@Nullable Boolean hardcore);

        @Nullable
        @Contract(pure = true)
        Boolean structures();

        @Contract(mutates = "this")
        Builder structures(@Nullable Boolean structures);

        @Nullable
        @Contract(pure = true)
        Boolean bonusChest();

        @Contract(mutates = "this")
        Builder bonusChest(@Nullable Boolean bonusChest);

        @Nullable
        @Contract(pure = true)
        GeneratorType generatorType();

        @Contract(mutates = "this")
        Builder generatorType(@Nullable GeneratorType generatorType);

        @Nullable
        @Contract(pure = true)
        Generator generator();

        @Contract(mutates = "this")
        Builder generator(@Nullable Generator generator);

        @Nullable
        @Contract(pure = true)
        Preset preset();

        @Contract(mutates = "this")
        Builder preset(@Nullable Preset preset);

        @Contract(pure = true)
        Level build();
    }
}
