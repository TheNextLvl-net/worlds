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
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

public sealed interface Level extends Keyed permits SimpleLevel {
    Path getDirectory();
    
    @Contract(pure = true)
    String getName();

    @Contract(pure = true)
    Environment getEnvironment();

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

    CompletableFuture<World> create();

    // todo: Stuff that has to go

    @ApiStatus.Experimental
    TriState isEnabled();

    @ApiStatus.Experimental
    boolean ignoreLevelData();

    @ApiStatus.Experimental
    TriState initialized();

    // todo end

    @Contract(pure = true)
    Builder toBuilder();

    static Builder builder(final Key key) {
        return new SimpleLevel.Builder(key);
    }

    sealed interface Builder permits SimpleLevel.Builder {
        @Contract(pure = true)
        Key key();

        @Contract(mutates = "this")
        Builder key(Key key);

        @Contract(pure = true)
        Optional<String> name();

        @Contract(mutates = "this")
        Builder name(@Nullable String name);

        @Contract(pure = true)
        Optional<Environment> environment();

        @Contract(mutates = "this")
        Builder environment(@Nullable Environment environment);

        @Contract(pure = true)
        OptionalLong seed();

        @Contract(mutates = "this")
        Builder seed(@Nullable Long seed);

        @Contract(pure = true)
        Optional<Boolean> hardcore();

        @Contract(mutates = "this")
        Builder hardcore(@Nullable Boolean hardcore);

        @Contract(pure = true)
        Optional<Boolean> structures();

        @Contract(mutates = "this")
        Builder structures(@Nullable Boolean structures);

        @Contract(pure = true)
        Optional<Boolean> bonusChest();

        @Contract(mutates = "this")
        Builder bonusChest(@Nullable Boolean bonusChest);

        @Contract(pure = true)
        Optional<GeneratorType> generatorType();

        @Contract(mutates = "this")
        Builder generatorType(@Nullable GeneratorType generatorType);

        @Contract(pure = true)
        Optional<Generator> generator();

        @Contract(mutates = "this")
        Builder generator(@Nullable Generator generator);

        @Contract(pure = true)
        Optional<Preset> preset();

        @Contract(mutates = "this")
        Builder preset(@Nullable Preset preset);

        @Contract(pure = true)
        Level build();
    }
}
