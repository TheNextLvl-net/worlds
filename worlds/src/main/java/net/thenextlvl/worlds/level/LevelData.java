package net.thenextlvl.worlds.level;

import core.nbt.tag.ByteTag;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.LongTag;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.preset.Preset;
import org.intellij.lang.annotations.Subst;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public abstract class LevelData implements Level {
    protected final WorldsPlugin plugin;
    protected final Path file;

    protected final Key key;
    protected final String name;
    protected final LevelStem levelStem;
    protected final GeneratorType generatorType;

    protected final @Nullable Generator generator;
    protected final @Nullable Preset preset;

    protected final TriState keepSpawnLoaded = TriState.NOT_SET;

    protected final TriState enabled;
    protected final boolean hardcore;
    protected final boolean worldKnown;
    protected final boolean structures;
    protected final boolean bonusChest;
    protected final long seed;

    protected LevelData(WorldsPlugin plugin, Builder builder) {
        this.plugin = plugin;
        this.file = builder.directory;
        this.name = builder.name != null ? builder.name : file.getFileName().toString();
        this.key = builder.key != null ? builder.key : createKey(name);
        this.levelStem = builder.levelStem != null ? builder.levelStem : getLevelStem(plugin, file);
        this.generatorType = builder.generatorType != null ? builder.generatorType : GeneratorType.NORMAL;
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
    public Path getFile() {
        return file;
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
    public Optional<Generator> getGenerator() {
        return Optional.ofNullable(generator);
    }

    @Override
    public TriState isKeepSpawnLoaded() {
        return keepSpawnLoaded;
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
        return new Builder(plugin, file)
                .key(key)
                .name(name)
                .levelStem(levelStem)
                .generatorType(generatorType)
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
        private final Path directory;

        private TriState keepSpawnLoaded = TriState.NOT_SET;

        private @Nullable Key key;
        private @Nullable String name;
        private @Nullable LevelStem levelStem;
        private @Nullable GeneratorType generatorType;
        private @Nullable Generator generator;
        private @Nullable Preset preset;
        private TriState enabled = TriState.NOT_SET;
        private @Nullable Boolean hardcore;
        private @Nullable Boolean structures;
        private @Nullable Boolean bonusChest;
        private @Nullable Boolean worldKnown;
        private @Nullable Long seed;

        public Builder(WorldsPlugin plugin, Path directory) {
            var container = plugin.getServer().getWorldContainer().toPath();
            this.directory = directory.startsWith(container) ? directory : container.resolve(directory);
            this.plugin = plugin;
        }

        @Override
        public Path directory() {
            return directory;
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
        public TriState keepSpawnLoaded() {
            return keepSpawnLoaded;
        }

        @Override
        public Level.Builder keepSpawnLoaded(TriState keepSpawnLoaded) {
            this.keepSpawnLoaded = keepSpawnLoaded;
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
            return plugin.isRunningFolia() ? new FoliaLevel(plugin, this) : new PaperLevel(plugin, this);
        }
    }

    @Override
    public String toString() {
        return "PaperLevelData{" +
               "file=" + file +
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

    public static Optional<Level> read(WorldsPlugin plugin, Path directory) {
        var container = plugin.getServer().getWorldContainer().toPath();
        var level = directory.startsWith(container) ? directory : container.resolve(directory);
        
        var levelData = plugin.levelView().getLevelDataFile(level).orElse(null);
        if (levelData == null) return Optional.empty();

        var data = levelData.getRoot().<CompoundTag>optional("Data");
        var name = data.flatMap(tag -> tag.optional("LevelName").map(Tag::getAsString))
                .orElseGet(() -> level.getFileName().toString());
        var pdc = data.flatMap(tag -> tag.optional("BukkitValues").map(Tag::getAsCompound));
        var worldKnown = pdc.map(LevelData::isKnown).orElse(false);
        var key = pdc.flatMap(tag -> tag.optional("worlds:world_key")
                .map(Tag::getAsString).map(Key::key)).orElseGet(() -> createKey(name));
        var enabled = pdc.flatMap(tag -> tag.optional("worlds:enabled").map(Tag::getAsBoolean)
                .map(TriState::byBoolean)).orElse(TriState.NOT_SET);
        var chunkGenerator = pdc.flatMap(tag -> tag.optional("worlds:generator").map(Tag::getAsString)).map(serialized ->
                Generator.deserialize(plugin, serialized)).orElse(null);
        var levelStem = getLevelStem(plugin, level);
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
        var worldPreset = generator.flatMap(plugin.levelView()::getWorldPreset);
        var preset = worldPreset.filter(type -> type.equals(GeneratorType.FLAT))
                .flatMap(worldType -> generator.flatMap(plugin.levelView()::getFlatPreset))
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
                .seed(seed)
                .build());
    }

    private static LevelStem getLevelStem(WorldsPlugin plugin, Path directory) {
        var end = plugin.levelView().hasEndDimension(directory);
        var nether = plugin.levelView().hasNetherDimension(directory);
        if (end && !nether) return LevelStem.END;
        if (nether && !end) return LevelStem.NETHER;
        return LevelStem.OVERWORLD;
    }

    private static boolean isKnown(CompoundTag tag) {
        return tag.containsKey("worlds:world_key") || tag.containsKey("worlds:enabled") || tag.containsKey("worlds:generator");
    }

    private static Key createKey(String name) {
        @Subst("pattern") var namespace = name.toLowerCase()
                .replace("(", "").replace(")", "")
                .replace(" ", "_");
        return Key.key("worlds", namespace);
    }
}
