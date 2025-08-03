package net.thenextlvl.worlds.api.generator;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

/**
 * @since 3.0.0
 */
@NullMarked
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface BiomeSource extends Keyed {
    /**
     * Creates a CheckerboardColumnBiomeSource instance using the specified biomes.
     * This biome source generates a checkerboard-like world layout with a pattern
     * based on the provided set of biomes.
     *
     * @param biomes the set of biomes to use for the checkerboard pattern
     * @return a new CheckerboardColumnBiomeSource configured with the specified biomes
     */
    @Contract(value = "_ -> new", pure = true)
    static CheckerboardColumnBiomeSource checkerboard(Set<Key> biomes) {
        return new CheckerboardColumnBiomeSource(biomes);
    }

    /**
     * Creates a FixedBiomeSource instance with the specified biome.
     * This biome source generates a world consisting entirely of the given biome.
     *
     * @param biome the biome to use for the world
     * @return a new FixedBiomeSource configured with the specified biome
     */
    @Contract(value = "_ -> new", pure = true)
    static FixedBiomeSource fixed(Key biome) {
        return new FixedBiomeSource(biome);
    }

    /**
     * Represents the "fixed" biome source.
     * <p>
     * This preset generates a world consisting of only one biome.
     * <a href="https://minecraft.wiki/w/Dimension_definition#fixed">Wiki</a>
     */
    final class FixedBiomeSource implements BiomeSource {
        private final Key key = Key.key("minecraft", "fixed");
        private final Key biome;

        /**
         * Constructs a new FixedBiomeSource with the specified biome.
         *
         * @param biome the biome to use for the world
         */
        private FixedBiomeSource(Key biome) {
            this.biome = biome;
        }

        /**
         * Gets the biome key associated with this biome source.
         *
         * @return the biome key
         */
        @Contract(pure = true)
        public Key biome() {
            return biome;
        }

        @Override
        @Contract(pure = true)
        public Key key() {
            return key;
        }
    }

    /**
     * Represents the "checkerboard" biome source.
     * <p>
     * This biome source generates a unique checkerboard-like pattern of biomes, creating a grid-like world layout.
     * <a href="https://minecraft.wiki/w/Dimension_definition#checkerboard">Wiki</a>
     */
    final class CheckerboardColumnBiomeSource implements BiomeSource {
        private final Key key = Key.key("minecraft", "checkerboard");
        private final Set<Key> biomes;

        /**
         * Constructs a new CheckerboardColumnBiomeSource with the specified biomes.
         *
         * @param biomes the set of biomes to use for the checkerboard pattern
         */
        private CheckerboardColumnBiomeSource(Set<Key> biomes) {
            this.biomes = Set.copyOf(biomes);
        }

        /**
         * Gets the set of biomes associated with this biome source.
         *
         * @return an unmodifiable set of biome keys
         */
        @Contract(pure = true)
        public @Unmodifiable Set<Key> biomes() {
            return Set.copyOf(biomes);
        }

        @Override
        @Contract(pure = true)
        public Key key() {
            return key;
        }
    }
}
