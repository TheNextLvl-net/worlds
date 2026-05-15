package net.thenextlvl.worlds.generator;

import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.preset.Preset;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

non-sealed class SimpleGeneratorType implements GeneratorType {
    private final Key key;
    private final String name;

    SimpleGeneratorType(final Key key, final String name) {
        this.key = key;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public String toString() {
        return "SimpleGeneratorType{" +
                "key=" + key +
                ", name='" + name + '\'' +
                '}';
    }

    static final class Flat extends SimpleGeneratorType implements GeneratorType.Flat {
        private final Preset preset;

        Flat(final Preset preset) {
            super(Key.key("minecraft", "flat"), "flat");
            this.preset = preset;
        }

        @Override
        public GeneratorType.Flat with(final Preset preset) {
            return new SimpleGeneratorType.Flat(preset);
        }

        @Override
        public Preset preset() {
            return preset;
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            final var flat = (SimpleGeneratorType.Flat) o;
            return Objects.equals(preset, flat.preset);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(preset);
        }

        @Override
        public String toString() {
            return "Flat{" +
                    "preset=" + preset +
                    '}';
        }
    }

    static final class SingleBiome extends SimpleGeneratorType implements GeneratorType.SingleBiome {
        private final Key biome;

        SingleBiome(final Key biome) {
            super(Key.key("minecraft", "fixed"), "single-biome");
            this.biome = biome;
        }

        @Override
        public GeneratorType.SingleBiome with(final Key biome) {
            return new SimpleGeneratorType.SingleBiome(biome);
        }

        @Override
        public Key biome() {
            return biome;
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            final var that = (SimpleGeneratorType.SingleBiome) o;
            return Objects.equals(biome, that.biome);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(biome);
        }

        @Override
        public String toString() {
            return "SingleBiome{" +
                    "biome=" + biome +
                    '}';
        }
    }
}
