package net.thenextlvl.worlds.level;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.nbt.tag.ByteTag;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.ListTag;
import net.thenextlvl.nbt.tag.LongTag;
import net.thenextlvl.nbt.tag.Tag;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.preset.Biome;
import net.thenextlvl.worlds.api.preset.Layer;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.api.preset.Structure;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.intellij.lang.annotations.Subst;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@NullMarked
public abstract class LevelData implements Level {
    protected final WorldsPlugin plugin;
    protected final Path directory;

    protected final Key key;
    protected final String name;
    protected final LevelStem levelStem;
    protected final GeneratorType generatorType;

    protected final @Nullable ChunkGenerator chunkGenerator;
    protected final @Nullable BiomeProvider biomeProvider;

    protected final @Nullable Generator generator;
    protected final @Nullable Preset preset;

    protected final TriState enabled;
    protected final boolean hardcore;
    protected final boolean worldKnown;
    protected final boolean structures;
    protected final boolean bonusChest;
    protected final long seed;

    protected LevelData(WorldsPlugin plugin, Builder builder) {
        this.plugin = plugin;
        this.directory = builder.directory;
        this.name = builder.name != null ? builder.name : directory.getFileName().toString();
        this.key = builder.key != null ? builder.key : Key.key("worlds", createKey(name));
        this.levelStem = builder.levelStem != null ? builder.levelStem : getLevelStem(plugin, directory);
        this.generatorType = builder.generatorType != null ? builder.generatorType
                : builder.preset != null ? GeneratorType.FLAT : GeneratorType.NORMAL;
        this.biomeProvider = builder.biomeProvider != null ? builder.biomeProvider
                : builder.generator != null ? builder.generator.biomeProvider(name) : null;
        this.chunkGenerator = builder.chunkGenerator != null ? builder.chunkGenerator
                : builder.generator != null ? builder.generator.generator(name) : null;
        this.generator = builder.generator;
        this.preset = builder.preset;
        this.enabled = builder.enabled;
        this.hardcore = builder.hardcore != null ? builder.hardcore : plugin.getServer().isHardcore();
        this.worldKnown = builder.worldKnown != null ? builder.worldKnown : false;
        this.structures = builder.structures != null ? builder.structures : plugin.getServer().getGenerateStructures();
        this.bonusChest = builder.bonusChest != null ? builder.bonusChest : false;
        this.seed = builder.seed != null ? builder.seed : ThreadLocalRandom.current().nextLong();
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public Path getDirectory() {
        return directory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GeneratorType getGeneratorType() {
        return generatorType;
    }

    @Override
    public LevelStem getLevelStem() {
        return levelStem;
    }

    @Override
    public Optional<Preset> getPreset() {
        return Optional.ofNullable(preset);
    }

    @Override
    public Optional<BiomeProvider> getBiomeProvider() {
        return Optional.ofNullable(biomeProvider);
    }

    @Override
    public Optional<ChunkGenerator> getChunkGenerator() {
        return Optional.ofNullable(chunkGenerator);
    }

    @Override
    public Optional<Generator> getGenerator() {
        return Optional.ofNullable(generator);
    }

    @Override
    public TriState isEnabled() {
        return enabled;
    }

    @Override
    public boolean isWorldKnown() {
        return worldKnown;
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
    public long getSeed() {
        return seed;
    }

    @Override
    public Level.Builder toBuilder() {
        return new Builder(plugin, directory)
                .key(key)
                .name(name)
                .levelStem(levelStem)
                .generatorType(generatorType)
                .biomeProvider(biomeProvider)
                .chunkGenerator(chunkGenerator)
                .generator(generator)
                .preset(preset)
                .enabled(enabled)
                .hardcore(hardcore)
                .worldKnown(worldKnown)
                .structures(structures)
                .bonusChest(bonusChest)
                .seed(seed);
    }

    public static class Builder implements Level.Builder {
        private final WorldsPlugin plugin;

        private @Nullable Boolean bonusChest;
        private @Nullable Boolean hardcore;
        private @Nullable Boolean structures;
        private @Nullable Boolean worldKnown;
        private @Nullable BiomeProvider biomeProvider;
        private @Nullable ChunkGenerator chunkGenerator;
        private @Nullable Generator generator;
        private @Nullable GeneratorType generatorType;
        private @Nullable Key key;
        private @Nullable LevelStem levelStem;
        private @Nullable Long seed;
        private @Nullable Preset preset;
        private @Nullable String name;

        private Path directory;
        private TriState enabled = TriState.NOT_SET;

        public Builder(WorldsPlugin plugin, Path directory) {
            this.plugin = plugin;
            this.directory = validate(directory);
        }

        private Path validate(Path directory) {
            var container = plugin.getServer().getWorldContainer().toPath();
            return directory.startsWith(container) ? directory : container.resolve(directory);
        }

        @Override
        public Path directory() {
            return directory;
        }

        @Override
        public Level.Builder directory(Path directory) {
            this.directory = validate(directory);
            return this;
        }

        @Override
        public @Nullable Key key() {
            return key;
        }

        @Override
        public Level.Builder key(@Nullable Key key) {
            this.key = key;
            return this;
        }

        @Override
        public @Nullable String name() {
            return name;
        }

        @Override
        public Level.Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        @Override
        public @Nullable BiomeProvider biomeProvider() {
            return biomeProvider;
        }

        @Override
        public Level.Builder biomeProvider(@Nullable BiomeProvider provider) {
            this.biomeProvider = provider;
            return this;
        }

        @Override
        public @Nullable ChunkGenerator chunkGenerator() {
            return chunkGenerator;
        }

        @Override
        public Level.Builder chunkGenerator(@Nullable ChunkGenerator generator) {
            this.chunkGenerator = generator;
            return this;
        }

        @Override
        public @Nullable LevelStem levelStem() {
            return levelStem;
        }

        @Override
        public Level.Builder levelStem(@Nullable LevelStem levelStem) {
            this.levelStem = levelStem;
            return this;
        }

        @Override
        public @Nullable GeneratorType generatorType() {
            return generatorType;
        }

        @Override
        public Level.Builder generatorType(@Nullable GeneratorType type) {
            this.generatorType = type;
            return this;
        }

        @Override
        public @Nullable Generator generator() {
            return generator;
        }

        @Override
        public Level.Builder generator(@Nullable Generator generator) {
            this.generator = generator;
            return this;
        }

        @Override
        public @Nullable Preset preset() {
            return preset;
        }

        @Override
        public Level.Builder preset(@Nullable Preset preset) {
            this.preset = preset;
            return this;
        }

        @Override
        public TriState enabled() {
            return enabled;
        }

        @Override
        public Level.Builder enabled(TriState enabled) {
            this.enabled = enabled;
            return this;
        }

        @Override
        public @Nullable Boolean hardcore() {
            return hardcore;
        }

        @Override
        public Level.Builder hardcore(@Nullable Boolean hardcore) {
            this.hardcore = hardcore;
            return this;
        }

        @Override
        public @Nullable Boolean structures() {
            return structures;
        }

        @Override
        public Level.Builder structures(@Nullable Boolean structures) {
            this.structures = structures;
            return this;
        }

        @Override
        public @Nullable Boolean bonusChest() {
            return bonusChest;
        }

        @Override
        public Level.Builder bonusChest(@Nullable Boolean bonusChest) {
            this.bonusChest = bonusChest;
            return this;
        }

        @Override
        public @Nullable Boolean worldKnown() {
            return worldKnown;
        }

        @Override
        public Level.Builder worldKnown(@Nullable Boolean worldKnown) {
            this.worldKnown = worldKnown;
            return this;
        }

        @Override
        public @Nullable Long seed() {
            return seed;
        }

        @Override
        public Level.Builder seed(@Nullable Long seed) {
            this.seed = seed;
            return this;
        }

        @Override
        public Level build() {
            return new PaperLevel(plugin, this);
        }
    }

    @Override
    public String toString() {
        return "LevelData{" +
               "directory=" + directory +
               ", key=" + key +
               ", name='" + name + '\'' +
               ", levelStem=" + levelStem +
               ", generatorType=" + generatorType.key() +
               ", generator=" + generator +
               ", preset=" + preset +
               ", enabled=" + enabled +
               ", hardcore=" + hardcore +
               ", worldKnown=" + worldKnown +
               ", structures=" + structures +
               ", bonusChest=" + bonusChest +
               ", seed=" + seed +
               '}';
    }

    public static Optional<Level.Builder> read(WorldsPlugin plugin, Path directory) throws IOException {
        var container = plugin.getServer().getWorldContainer().toPath();
        var level = directory.startsWith(container) ? directory : container.resolve(directory);

        var levelData = plugin.levelView().getLevelDataFile(level);
        if (levelData == null) return Optional.empty();

        var data = levelData.<CompoundTag>optional("Data");
        var name = data.flatMap(tag -> tag.optional("LevelName").map(Tag::getAsString))
                .orElseGet(() -> level.getFileName().toString());
        var pdc = data.flatMap(tag -> tag.optional("BukkitValues").map(Tag::getAsCompound));
        var worldKnown = pdc.map(LevelData::isKnown).orElse(false);
        var key = pdc.flatMap(tag -> tag.optional("worlds:world_key")
                .map(Tag::getAsString).map(Key::key)).orElseGet(() -> Key.key("worlds", createKey(name)));
        var levelStem = pdc.flatMap(tag -> tag.optional("worlds:dimension").map(Tag::getAsString))
                .map(Key::key).map(LevelStem::of).orElseGet(() -> getLevelStem(plugin, level));
        var enabled = pdc.flatMap(tag -> tag.optional("worlds:enabled").map(Tag::getAsBoolean)
                .map(TriState::byBoolean)).orElse(TriState.NOT_SET);
        var chunkGenerator = pdc.flatMap(tag -> tag.optional("worlds:generator").map(Tag::getAsString)).map(serialized ->
                Generator.of(plugin, serialized)).orElse(null);
        var settings = data.flatMap(tag -> tag.<CompoundTag>optional("WorldGenSettings"));
        var dimensions = settings.flatMap(tag -> tag.<CompoundTag>optional("dimensions"));
        var dimension = dimensions.flatMap(tag -> tag.<CompoundTag>optional(levelStem.dimensionType().key().asString()));
        var generator = dimension.flatMap(tag -> tag.<CompoundTag>optional("generator"));
        var hardcore = settings.flatMap(tag -> tag.<ByteTag>optional("hardcore"))
                .map(ByteTag::getAsBoolean).orElse(plugin.getServer().isHardcore());
        var seed = settings.flatMap(tag -> tag.<LongTag>optional("seed"))
                .map(LongTag::getAsLong).orElse(ThreadLocalRandom.current().nextLong());
        var structures = settings.flatMap(tag -> tag.<ByteTag>optional("generate_features"))
                .map(ByteTag::getAsBoolean).orElse(plugin.getServer().getGenerateStructures());
        var bonusChest = settings.flatMap(tag -> tag.<ByteTag>optional("bonus_chest"))
                .map(ByteTag::getAsBoolean).orElse(false);
        var worldPreset = generator.flatMap(LevelData::getWorldPreset);
        var preset = worldPreset.filter(type -> type.equals(GeneratorType.FLAT))
                .flatMap(worldType -> generator.flatMap(LevelData::getFlatPreset))
                .orElse(null);
        var generatorType = worldPreset.orElse(GeneratorType.NORMAL);

        return Optional.of(new Builder(plugin, level)
                .key(key)
                .name(name)
                .levelStem(levelStem)
                .generatorType(generatorType)
                .generator(chunkGenerator)
                .preset(preset)
                .enabled(enabled)
                .hardcore(hardcore)
                .structures(structures)
                .bonusChest(bonusChest)
                .worldKnown(worldKnown)
                .seed(seed));
    }

    @SuppressWarnings("PatternValidation")
    private static Optional<Preset> getFlatPreset(CompoundTag generator) {
        var settings = generator.<CompoundTag>optional("settings");

        if (settings.isEmpty()) return Optional.empty();

        var preset = new Preset(null);

        settings.flatMap(tag -> tag.<Tag>optional("biome"))
                .map(Tag::getAsString)
                .map(Biome::literal)
                .ifPresent(preset::biome);

        settings.flatMap(tag -> tag.<Tag>optional("features"))
                .map(Tag::getAsBoolean)
                .ifPresent(preset::features);

        settings.flatMap(tag -> tag.<Tag>optional("lakes"))
                .map(Tag::getAsBoolean)
                .ifPresent(preset::lakes);

        settings.flatMap(tag -> tag.<ListTag<CompoundTag>>optional("layers"))
                .map(tag -> tag.stream().map(layer -> {
                    var block = layer.optional("block").orElseThrow().getAsString();
                    var height = layer.optional("height").orElseThrow().getAsInt();
                    return new Layer(block, height);
                }).collect(Collectors.toCollection(LinkedHashSet::new)))
                .ifPresent(preset::layers);

        settings.flatMap(tag -> tag.optional("structure_overrides")
                        .filter(Tag::isList).map(Tag::getAsList))
                .map(list -> list.stream()
                        .map(Tag::getAsString)
                        .map(Structure::new)
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .ifPresent(preset::structures);
        settings.flatMap(tag -> tag.optional("structure_overrides")
                        .filter(Tag::isString).map(Tag::getAsString))
                .map(Structure::new)
                .ifPresent(preset::addStructure);

        return Optional.of(preset);
    }

    private static Optional<GeneratorType> getWorldPreset(CompoundTag generator) {
        var settings = generator.optional("settings").filter(Tag::isString).map(Tag::getAsString);
        if (settings.filter(s -> s.equals(GeneratorType.LARGE_BIOMES.key().asString())).isPresent())
            return Optional.of(GeneratorType.LARGE_BIOMES);
        if (settings.filter(s -> s.equals(GeneratorType.AMPLIFIED.key().asString())).isPresent())
            return Optional.of(GeneratorType.AMPLIFIED);

        var generatorType = generator.optional("type").map(Tag::getAsString);
        if (generatorType.filter(s -> s.equals(GeneratorType.DEBUG.key().asString())).isPresent())
            return Optional.of(GeneratorType.DEBUG);
        if (generatorType.filter(s -> s.equals(GeneratorType.FLAT.key().asString())).isPresent())
            return Optional.of(GeneratorType.FLAT);
        if (generatorType.filter(s -> s.equals(GeneratorType.NORMAL.key().asString())).isPresent())
            return Optional.of(GeneratorType.NORMAL);

        return Optional.empty();
    }

    private static LevelStem getLevelStem(WorldsPlugin plugin, Path directory) {
        if (Files.isDirectory(directory.resolve("region"))) return LevelStem.OVERWORLD;
        var end = plugin.levelView().hasEndDimension(directory);
        var nether = plugin.levelView().hasNetherDimension(directory);
        if (end && !nether) return LevelStem.END;
        if (nether && !end) return LevelStem.NETHER;
        return LevelStem.OVERWORLD;
    }

    private static boolean isKnown(CompoundTag tag) {
        return tag.containsKey("worlds:dimension")
               || tag.containsKey("worlds:enabled")
               || tag.containsKey("worlds:generator")
               || tag.containsKey("worlds:world_key");
    }

    public static @Subst("pattern") String createKey(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9_\\-./ ]+", "")
                .replace(" ", "_");
    }
}
