package net.thenextlvl.worlds.level;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import core.nbt.tag.ByteTag;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.LongTag;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.DimensionType;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.level.Level;
import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
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
    protected final DimensionType dimensionType;
    protected final GeneratorType generatorType;

    protected final @Nullable Generator generator;
    protected final @Nullable Preset preset;

    protected final boolean enabled;
    protected final boolean hardcore;
    protected final boolean worldKnown;
    protected final boolean structures;
    protected final boolean bonusChest;
    protected final long seed;

    protected LevelData(WorldsPlugin plugin, Builder builder) {
        Preconditions.checkState(builder.key != null, "Key must be set");
        Preconditions.checkState(builder.name != null, "Name must be set");
        Preconditions.checkState(builder.dimensionType != null, "Dimension type must be set");
        Preconditions.checkState(builder.generatorType != null, "Generator type must be set");
        Preconditions.checkState(builder.seed != null, "Seed must be set");
        Preconditions.checkState(builder.hardcore != null, "Hardcore must be set");
        Preconditions.checkState(builder.structures != null, "Structures must be set");
        Preconditions.checkState(builder.bonusChest != null, "Bonus chest must be set");

        this.plugin = plugin;
        this.file = builder.directory;
        this.key = builder.key;
        this.name = builder.name;
        this.dimensionType = builder.dimensionType;
        this.generatorType = builder.generatorType;
        this.generator = builder.generator;
        this.preset = builder.preset;
        this.enabled = true;
        this.hardcore = builder.hardcore;
        this.worldKnown = false;
        this.structures = builder.structures;
        this.bonusChest = builder.bonusChest;
        this.seed = builder.seed;
    }

    @Override
    public Path getFile() {
        return file;
    }

    @Override
    public Key getKey() {
        return key;
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
    public DimensionType getDimensionType() {
        return dimensionType;
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
    public boolean isEnabled() {
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
    public Optional<World> create() {
        var generatorSettings = Optional.ofNullable(preset)
                .map(Preset::serialize)
                .map(JsonObject::toString)
                .orElse("");

        var creator = new WorldCreator(name, new NamespacedKey(key.namespace(), key.value()))
                .environment(dimensionType.toBukkit())
                .generateStructures(structures)
                .generatorSettings(generatorSettings)
                .hardcore(hardcore)
                .seed(seed)
                .bonusChest(bonusChest)
                .type(typeOf(generatorType));

        if (generator != null) creator.generator(generator.generator(creator.name()));
        if (generator != null) creator.biomeProvider(generator.biomeProvider(creator.name()));

        return Optional.ofNullable(createWorld(creator));
    }

    protected abstract @Nullable World createWorld(WorldCreator creator);

    private WorldType typeOf(GeneratorType generatorType) {
        if (generatorType.equals(GeneratorType.AMPLIFIED)) return WorldType.AMPLIFIED;
        if (generatorType.equals(GeneratorType.FLAT)) return WorldType.FLAT;
        if (generatorType.equals(GeneratorType.LARGE_BIOMES)) return WorldType.LARGE_BIOMES;
        if (generatorType.equals(GeneratorType.NORMAL)) return WorldType.NORMAL;
        plugin.getComponentLogger().warn("Custom world presets do not work yet, defaulting to normal");
        return WorldType.NORMAL;
    }

    @Override
    public Key key() {
        return key;
    }

    public static class Builder implements Level.Builder {
        private final WorldsPlugin plugin;
        private final Path directory;

        private @Nullable Key key;
        private @Nullable String name;
        private @Nullable DimensionType dimensionType;
        private @Nullable GeneratorType generatorType;
        private @Nullable Generator generator;
        private @Nullable Preset preset;
        private @Nullable Boolean enabled;
        private @Nullable Boolean hardcore;
        private @Nullable Boolean structures;
        private @Nullable Boolean bonusChest;
        private @Nullable Long seed;

        public Builder(WorldsPlugin plugin, Path directory) {
            this.directory = directory;
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
        public @Nullable DimensionType dimensionType() {
            return dimensionType;
        }

        @Override
        public Level.Builder dimensionType(@Nullable DimensionType type) {
            this.dimensionType = type;
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
        public @Nullable Boolean enabled() {
            return enabled;
        }

        @Override
        public Level.Builder enabled(@Nullable Boolean enabled) {
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
               ", dimensionType=" + dimensionType.key() +
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
        var levelData = plugin.levelView().getLevelDataFile(directory).orElse(null);
        if (levelData == null) return Optional.empty();

        var data = levelData.getRoot().<CompoundTag>optional("Data");
        var name = data.flatMap(tag -> tag.optional("LevelName").map(Tag::getAsString))
                .orElseGet(() -> directory.getFileName().toString());
        var pdc = data.flatMap(tag -> tag.optional("BukkitValues").map(Tag::getAsCompound));
        var worldKnown = pdc.map(LevelData::isKnown).orElse(false);
        var key = pdc.flatMap(tag -> tag.optional("worlds:world_key")
                .map(Tag::getAsString).map(Key::key)).orElseGet(() -> createKey(name));
        var enabled = pdc.flatMap(tag -> tag.optional("worlds:enabled").map(Tag::getAsBoolean)).orElse(false);
        var generator = pdc.flatMap(tag -> tag.optional("worlds:generator").map(Tag::getAsString)).map(serialized ->
                Generator.deserialize(plugin, serialized)).orElse(null);
        var dimensionType = getDimensionType(plugin, directory);
        var settings = data.flatMap(tag -> tag.<CompoundTag>optional("WorldGenSettings"));
        var dimensions = settings.flatMap(tag -> tag.<CompoundTag>optional("dimensions"));
        var dimension = dimensions.flatMap(tag -> tag.<CompoundTag>optional(dimensionType.key().asString()));
        var generatorType = dimension.flatMap(tag -> tag.<CompoundTag>optional("generator"));
        var hardcore = settings.flatMap(tag -> tag.<ByteTag>optional("hardcore"))
                .map(ByteTag::getAsBoolean).orElse(plugin.getServer().isHardcore());
        var seed = settings.flatMap(tag -> tag.<LongTag>optional("seed"))
                .map(LongTag::getAsLong).orElse(ThreadLocalRandom.current().nextLong());
        var structures = settings.flatMap(tag -> tag.<ByteTag>optional("generate_features"))
                .map(ByteTag::getAsBoolean).orElse(plugin.getServer().getGenerateStructures());
        var bonusChest = settings.flatMap(tag -> tag.<ByteTag>optional("bonus_chest"))
                .map(ByteTag::getAsBoolean).orElse(false);
        var worldPreset = generatorType.flatMap(plugin.levelView()::getWorldPreset);
        var preset = worldPreset.filter(type -> type.equals(GeneratorType.FLAT))
                .flatMap(worldType -> generatorType.flatMap(plugin.levelView()::getFlatPreset))
                .orElse(null);

        return Optional.of(new Builder(plugin, directory)
                .key(key)
                .name(name)
                .dimensionType(dimensionType)
                .generator(generator)
                .bonusChest(bonusChest)
                .structures(structures)
                .preset(preset)
                .seed(seed)
                .hardcore(hardcore)
                .build());
    }

    private static DimensionType getDimensionType(WorldsPlugin plugin, Path directory) {
        var end = plugin.levelView().hasEndDimension(directory);
        var nether = plugin.levelView().hasNetherDimension(directory);
        if (end && nether) return DimensionType.OVERWORLD;
        if (end) return DimensionType.THE_END;
        if (nether) return DimensionType.THE_NETHER;
        return DimensionType.OVERWORLD;
    }

    private static boolean isKnown(CompoundTag tag) {
        return tag.containsKey("worlds:world_key") || tag.containsKey("worlds:enabled") || tag.containsKey("worlds:generator");
    }

    private static NamespacedKey createKey(String name) {
        var namespace = name.toLowerCase()
                .replace("(", "").replace(")", "")
                .replace(" ", "_");
        return new NamespacedKey("worlds", namespace);
    }
}
