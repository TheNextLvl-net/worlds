package net.thenextlvl.worlds;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.worlds.experimental.GeneratorType;
import net.thenextlvl.worlds.generator.Generator;
import net.thenextlvl.worlds.preset.Preset;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

final class SimpleLevel implements Level {
    private final Key key;
    private final String name;

    private final Environment environment;
    private final GeneratorType generatorType;

    private final @Nullable BiomeProvider biomeProvider;
    private final @Nullable ChunkGenerator chunkGenerator;

    private final @Nullable Generator generator;
    private final @Nullable Preset preset;

    private final TriState enabled = TriState.NOT_SET;
    private final TriState initialized = TriState.NOT_SET;
    private final boolean ignoreLevelData = false;

    private final boolean bonusChest;
    private final boolean hardcore;
    private final boolean structures;
    private final long seed;

    SimpleLevel(final Builder builder) {
        final var server = WorldsAccess.access().getServer();

        this.key = builder.key;
        this.name = builder.name().orElseGet(() -> builder.key().value());

        this.environment = builder.environment().orElse(Environment.OVERWORLD);

        this.hardcore = builder.hardcore().orElseGet(server::isHardcore);
        this.seed = builder.seed().orElseGet(ThreadLocalRandom.current()::nextLong);
        this.structures = builder.structures().orElseGet(server::getGenerateStructures);

        this.bonusChest = builder.bonusChest().orElse(false);

        this.generator = builder.generator;
        this.preset = builder.preset;

        this.biomeProvider = builder.generator().flatMap(generator -> generator.biomeProvider(name)).orElse(null);
        this.chunkGenerator = builder.generator().flatMap(generator -> generator.generator(name)).orElse(null);

        this.generatorType = builder.generatorType().orElseGet(() -> {
            return builder.preset().isPresent() ? GeneratorType.FLAT : GeneratorType.NORMAL;
        });
    }

    @Override
    public Path getDirectory() {
        // todo: move to plugin? version dependant?
        return WorldsAccess.access().getWorldContainer().resolve(key.namespace()).resolve(key.value());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public boolean isHardcore() {
        return hardcore;
    }

    @Override
    public boolean hasStructures() {
        return structures;
    }

    @Override
    public boolean hasBonusChest() {
        return bonusChest;
    }

    @Override
    public GeneratorType getGeneratorType() {
        return generatorType;
    }

    @Override
    public Optional<Generator> getGenerator() {
        return Optional.ofNullable(generator);
    }

    @Override
    public Optional<ChunkGenerator> getChunkGenerator() {
        return Optional.ofNullable(chunkGenerator);
    }

    @Override
    public Optional<BiomeProvider> getBiomeProvider() {
        return Optional.ofNullable(biomeProvider);
    }

    @Override
    public Optional<Preset> getPreset() {
        return Optional.ofNullable(preset);
    }

    @Override
    public CompletableFuture<World> create() {
        return WorldsAccess.access().create(this);
    }

    @Override
    public TriState isEnabled() {
        return enabled;
    }

    @Override
    public boolean ignoreLevelData() {
        return ignoreLevelData;
    }

    @Override
    public TriState initialized() {
        return initialized;
    }

    @Override
    public Level.Builder toBuilder() {
        return new Builder(key)
                .bonusChest(bonusChest)
                .environment(environment)
                .generator(generator)
                .generatorType(generatorType)
                .hardcore(hardcore)
                .name(name)
                .preset(preset)
                .seed(seed)
                .structures(structures);
    }

    @Override
    public Key key() {
        return key;
    }

    static final class Builder implements Level.Builder {
        private @Nullable Boolean bonusChest;
        private @Nullable Boolean hardcore;
        private @Nullable Boolean structures;
        private @Nullable Environment environment;
        private @Nullable Generator generator;
        private @Nullable GeneratorType generatorType;
        private @Nullable Long seed;
        private @Nullable Preset preset;
        private @Nullable String name;
        private Key key;

        public Builder(final Key key) {
            this.key = key;
        }

        @Override
        public Optional<String> name() {
            return Optional.ofNullable(name);
        }

        @Override
        public Level.Builder name(@Nullable final String name) {
            this.name = name;
            return this;
        }

        @Override
        public Optional<Environment> environment() {
            return Optional.ofNullable(environment);
        }

        @Override
        public Level.Builder environment(@Nullable final Environment environment) {
            this.environment = environment;
            return this;
        }

        @Override
        public Key key() {
            return key;
        }

        @Override
        public Level.Builder key(final Key key) {
            this.key = key;
            return this;
        }

        @Override
        public OptionalLong seed() {
            return seed != null ? OptionalLong.of(seed) : OptionalLong.empty();
        }

        @Override
        public Level.Builder seed(@Nullable final Long seed) {
            this.seed = seed;
            return this;
        }

        @Override
        public Optional<Boolean> hardcore() {
            return Optional.ofNullable(hardcore);
        }

        @Override
        public Level.Builder hardcore(@Nullable final Boolean hardcore) {
            this.hardcore = hardcore;
            return this;
        }

        @Override
        public Optional<Boolean> structures() {
            return Optional.ofNullable(structures);
        }

        @Override
        public Level.Builder structures(@Nullable final Boolean structures) {
            this.structures = structures;
            return this;
        }

        @Override
        public Optional<Boolean> bonusChest() {
            return Optional.ofNullable(bonusChest);
        }

        @Override
        public Level.Builder bonusChest(@Nullable final Boolean bonusChest) {
            this.bonusChest = bonusChest;
            return this;
        }

        @Override
        public Optional<GeneratorType> generatorType() {
            return Optional.ofNullable(generatorType);
        }

        @Override
        public Level.Builder generatorType(@Nullable final GeneratorType generatorType) {
            this.generatorType = generatorType;
            return this;
        }

        @Override
        public Optional<Generator> generator() {
            return Optional.ofNullable(generator);
        }

        @Override
        public Level.Builder generator(@Nullable final Generator generator) {
            this.generator = generator;
            return this;
        }

        @Override
        public Optional<Preset> preset() {
            return Optional.ofNullable(preset);
        }

        @Override
        public Level.Builder preset(@Nullable final Preset preset) {
            this.preset = preset;
            return this;
        }

        @Override
        public Level build() {
            return new SimpleLevel(this);
        }
    }
}
