package net.thenextlvl.worlds.model;

import com.google.gson.JsonObject;
import core.nbt.file.NBTFile;
import core.nbt.tag.ByteTag;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.LongTag;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.model.Generator;
import net.thenextlvl.worlds.api.model.Level;
import net.thenextlvl.worlds.api.model.LevelBuilder;
import net.thenextlvl.worlds.api.model.LevelExtras;
import net.thenextlvl.worlds.api.model.WorldPreset;
import net.thenextlvl.worlds.api.preset.Preset;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public class PaperLevel implements Level {
    protected final NBTFile<CompoundTag> levelData;
    protected final WorldsPlugin plugin;

    protected final NamespacedKey key;
    protected final String name;
    protected final World.Environment environment;
    protected final WorldPreset type;

    protected final @Nullable Generator generator;
    protected final @Nullable Preset preset;

    protected final boolean enabled;
    protected final boolean hardcore;
    protected final boolean importedBefore;
    protected final boolean structures;
    protected final boolean bonusChest;
    protected final long seed;

    public PaperLevel(WorldsPlugin plugin, LevelBuilder builder) {
        this.levelData = plugin.levelView().getLevelDataFile(builder.level());
        this.plugin = plugin;

        this.name = Optional.ofNullable(builder.name())
                .orElseGet(() -> builder.level().getName());

        var data = levelData.getRoot().<CompoundTag>optional("Data");
        var extras = data.flatMap(plugin.levelView()::getExtras);

        this.importedBefore = extras.isPresent();
        this.enabled = extras.filter(LevelExtras::enabled).isPresent();

        this.environment = Optional.ofNullable(builder.environment())
                .orElseGet(() -> plugin.levelView().getEnvironment(builder.level()));

        this.key = Optional.ofNullable(builder.key())
                .or(() -> extras.map(LevelExtras::key))
                .orElseGet(() -> {
                    var namespace = builder.level().getName().toLowerCase()
                            .replace("(", "").replace(")", "")
                            .replace(" ", "_");
                    return new NamespacedKey("worlds", namespace);
                });

        var settings = data.flatMap(tag -> tag.<CompoundTag>optional("WorldGenSettings"));
        var dimensions = settings.flatMap(tag -> tag.<CompoundTag>optional("dimensions"));
        var dimension = dimensions.flatMap(tag -> tag.<CompoundTag>optional(
                plugin.levelView().getDimension(tag, environment)));
        var generator = dimension.flatMap(tag -> tag.<CompoundTag>optional("generator"));


        this.hardcore = Optional.ofNullable(builder.hardcore())
                .or(() -> settings.flatMap(tag -> tag.<ByteTag>optional("hardcore"))
                        .map(ByteTag::getAsBoolean))
                .orElse(plugin.getServer().isHardcore());
        this.seed = Optional.ofNullable(builder.seed())
                .or(() -> settings.flatMap(tag -> tag.<LongTag>optional("seed"))
                        .map(LongTag::getAsLong))
                .orElse(ThreadLocalRandom.current().nextLong());
        this.structures = Optional.ofNullable(builder.structures())
                .or(() -> settings.flatMap(tag -> tag.<ByteTag>optional("generate_features"))
                        .map(ByteTag::getAsBoolean))
                .orElse(plugin.getServer().getGenerateStructures());
        this.bonusChest = Optional.ofNullable(builder.bonusChest())
                .or(() -> settings.flatMap(tag -> tag.<ByteTag>optional("bonus_chest"))
                        .map(ByteTag::getAsBoolean))
                .orElse(false);


        var worldPreset = generator.flatMap(plugin.levelView()::getWorldPreset);

        this.preset = Optional.ofNullable(builder.preset())
                .or(() -> worldPreset.filter(preset -> preset.equals(WorldPreset.FLAT))
                        .flatMap(worldType -> generator.flatMap(plugin.levelView()::getFlatPreset)))
                .orElse(null);

        this.type = Optional.ofNullable(builder.type()).orElseGet(() ->
                worldPreset.orElse(WorldPreset.NORMAL));

        this.generator = Optional.ofNullable(builder.generator())
                .or(() -> extras.map(LevelExtras::generator))
                .orElse(null);
    }

    @Override
    public @Nullable Generator generator() {
        return generator;
    }

    @Override
    public @Nullable Preset preset() {
        return preset;
    }

    @Override
    public NBTFile<CompoundTag> levelData() {
        return levelData;
    }

    @Override
    public NamespacedKey key() {
        return key;
    }

    @Override
    public Optional<World> create() {
        var generatorSettings = Optional.ofNullable(preset)
                .map(Preset::serialize)
                .map(JsonObject::toString)
                .orElse("");

        var creator = new WorldCreator(name, key)
                .environment(environment)
                .generateStructures(structures)
                .generatorSettings(generatorSettings)
                .hardcore(hardcore)
                .seed(seed)
                .bonusChest(bonusChest)
                .type(typeOf(type));

        if (generator != null) creator.generator(generator.generator(creator.name()));
        if (generator != null) creator.biomeProvider(generator.biomeProvider(creator.name()));

        return Optional.ofNullable(createWorld(creator));
    }

    protected @Nullable World createWorld(WorldCreator creator) {
        return creator.createWorld();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public World.Environment environment() {
        return environment;
    }

    @Override
    public WorldPreset type() {
        return type;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public boolean hardcore() {
        return hardcore;
    }

    @Override
    public boolean importedBefore() {
        return importedBefore;
    }

    @Override
    public boolean structures() {
        return structures;
    }

    @Override
    public boolean bonusChest() {
        return bonusChest;
    }

    @Override
    public long seed() {
        return seed;
    }

    private WorldType typeOf(WorldPreset worldPreset) {
        if (worldPreset.equals(WorldPreset.AMPLIFIED)) return WorldType.AMPLIFIED;
        if (worldPreset.equals(WorldPreset.FLAT)) return WorldType.FLAT;
        if (worldPreset.equals(WorldPreset.LARGE_BIOMES)) return WorldType.LARGE_BIOMES;
        if (worldPreset.equals(WorldPreset.NORMAL)) return WorldType.NORMAL;
        plugin.getComponentLogger().warn("Custom world presets do not work yet, defaulting to normal");
        return WorldType.NORMAL;
    }
}
