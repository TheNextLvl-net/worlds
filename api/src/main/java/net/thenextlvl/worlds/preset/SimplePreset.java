package net.thenextlvl.worlds.preset;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

final class SimplePreset implements Preset {
    private final @Nullable String name;
    private final Biome biome;
    private final boolean lakes;
    private final boolean features;
    private final boolean decoration;
    private final Set<Layer> layers;
    private final Set<Structure> structures;

    private SimplePreset(
            @Nullable final String name, final Biome biome, final boolean lakes, final boolean features,
            final boolean decoration, final Set<Layer> layers, final Set<Structure> structures
    ) {
        this.name = name;
        this.biome = biome;
        this.lakes = lakes;
        this.features = features;
        this.decoration = decoration;
        this.layers = layers;
        this.structures = structures;
    }

    @Override
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    @Override
    public Biome biome() {
        return biome;
    }

    @Override
    public boolean lakes() {
        return lakes;
    }

    @Override
    public boolean features() {
        return features;
    }

    @Override
    public boolean decoration() {
        return decoration;
    }

    @Override
    public @Unmodifiable Set<Layer> layers() {
        return layers;
    }

    @Override
    public @Unmodifiable Set<Structure> structures() {
        return structures;
    }

    @Override
    public String asString() {
        final var layers = this.layers.stream()
                .map(Layer::toString)
                .collect(Collectors.joining(","));
        return layers + ";" + biome();
    }

    @Override
    public JsonObject toJson() {
        final var root = new JsonObject();
        final var layers = new JsonArray();
        final var structures = new JsonArray();
        root.addProperty("name", name);
        root.addProperty("biome", biome.key().asString());
        root.addProperty("lakes", lakes);
        root.addProperty("features", features);
        root.addProperty("decoration", decoration);
        this.layers.forEach(layer -> {
            final var object = new JsonObject();
            object.addProperty("block", layer.block().key().asString());
            object.addProperty("height", layer.height());
            layers.add(object);
        });
        this.structures.forEach(structure -> structures.add(structure.key().asString()));
        root.add("layers", layers);
        root.add("structure_overrides", structures);
        return root;
    }

    @Override
    public Preset.Builder toBuilder() {
        return Preset.builder()
                .name(name)
                .biome(biome)
                .lakes(lakes)
                .features(features)
                .decoration(decoration)
                .layers(layers)
                .structures(structures);
    }

    public static final class Builder implements Preset.Builder {
        private @Nullable String name;
        private Biome biome = Biome.literal("minecraft:plains");
        private Set<Layer> layers = new LinkedHashSet<>();
        private Set<Structure> structures = new LinkedHashSet<>();
        private boolean decoration;
        private boolean features;
        private boolean lakes;

        @Override
        public Preset.Builder name(@Nullable final String name) {
            this.name = name;
            return this;
        }

        @Override
        public Preset.Builder biome(final Biome biome) {
            this.biome = biome;
            return this;
        }

        @Override
        public Preset.Builder decoration(final boolean decoration) {
            this.decoration = decoration;
            return this;
        }

        @Override
        public Preset.Builder features(final boolean features) {
            this.features = features;
            return this;
        }

        @Override
        public Preset.Builder lakes(final boolean lakes) {
            this.lakes = lakes;
            return this;
        }

        @Override
        public Preset.Builder layers(final Set<Layer> layers) {
            this.layers = new LinkedHashSet<>(layers);
            return this;
        }

        @Override
        public Preset.Builder addLayer(final Layer layer) {
            this.layers.add(layer);
            return this;
        }

        @Override
        public Preset.Builder structures(final Set<Structure> structures) {
            this.structures = new LinkedHashSet<>(structures);
            return this;
        }

        @Override
        public Preset.Builder addStructure(final Structure structure) {
            this.structures.add(structure);
            return this;
        }

        @Override
        public Preset build() {
            return new SimplePreset(
                    name, biome, lakes, features, decoration,
                    Set.copyOf(layers), Set.copyOf(structures)
            );
        }
    }

    @SuppressWarnings("PatternValidation")
    public static Preset fromString(final String presetCode) {
        final var strings = presetCode.split(";", 2);
        final var layers = Arrays.stream(strings[0].split(",")).map(layer -> {
            final var parameters = layer.split("\\*", 2);
            final var material = parameters.length == 1 ? parameters[0] : parameters[1];
            final var height = parameters.length == 1 ? 1 : Integer.parseInt(parameters[0]);
            final var matched = Material.matchMaterial(material);
            if (matched != null) return new Layer(matched, height);
            throw new IllegalArgumentException("Invalid material: " + material);
        }).collect(Collectors.toCollection(LinkedHashSet::new));
        return Preset.builder().layers(layers).biome(Biome.literal(strings[1])).build();
    }

    @SuppressWarnings("PatternValidation")
    public static Preset fromJson(final JsonObject object) throws IllegalArgumentException {
        Preconditions.checkArgument(object.has("layers"), "Missing layers");
        final var preset = Preset.builder().name(object.has("name") ? object.get("name").getAsString() : null);
        if (object.has("biome")) preset.biome(Biome.literal(object.get("biome").getAsString()));
        if (object.has("lakes")) preset.lakes(object.get("lakes").getAsBoolean());
        if (object.has("features")) preset.features(object.get("features").getAsBoolean());
        if (object.has("decoration")) preset.decoration(object.get("decoration").getAsBoolean());
        object.getAsJsonArray("layers").forEach(layer -> {
            final var layerObject = layer.getAsJsonObject();
            final var material = Material.matchMaterial(layerObject.get("block").getAsString());
            final var height = layerObject.get("height").getAsInt();
            if (material != null) preset.addLayer(new Layer(material, height));
        });
        if (object.has("structure_overrides")) object.getAsJsonArray("structure_overrides")
                .forEach(structure -> preset.addStructure(Structure.literal(structure.getAsString())));
        return preset.build();
    }

    static final Preset BOTTOMLESS_PIT = Preset.builder()
            .name("Bottomless Pit")
            .addLayer(new Layer(Material.COBBLESTONE, 2))
            .addLayer(new Layer(Material.DIRT, 3))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.literal("villages"))
            .build();

    static final Preset CLASSIC_FLAT = Preset.builder()
            .name("Classic Flat")
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.DIRT, 2))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.literal("villages"))
            .build();

    static final Preset DESERT = Preset.builder()
            .name("Desert")
            .biome(Biome.literal("desert"))
            .features(true)
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 3))
            .addLayer(new Layer(Material.SANDSTONE, 52))
            .addLayer(new Layer(Material.SAND, 8))
            .addStructure(Structure.literal("desert_pyramids"))
            .addStructure(Structure.literal("mineshafts"))
            .addStructure(Structure.literal("strongholds"))
            .addStructure(Structure.literal("villages"))
            .build();

    static final Preset OVERWORLD = Preset.builder()
            .name("Overworld")
            .lakes(true)
            .features(true)
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 59))
            .addLayer(new Layer(Material.DIRT, 3))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.literal("mineshafts"))
            .addStructure(Structure.literal("pillager_outposts"))
            .addStructure(Structure.literal("ruined_portals"))
            .addStructure(Structure.literal("strongholds"))
            .addStructure(Structure.literal("villages"))
            .build();

    static final Preset REDSTONE_READY = Preset.builder()
            .name("Redstone Ready")
            .biome(Biome.literal("desert"))
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 3))
            .addLayer(new Layer(Material.SANDSTONE, 116))
            .build();

    static final Preset SNOWY_KINGDOM = Preset.builder()
            .name("Snowy Kingdom")
            .biome(Biome.literal("snowy_plains"))
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 59))
            .addLayer(new Layer(Material.DIRT, 3))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addLayer(new Layer(Material.SNOW, 1))
            .addStructure(Structure.literal("igloos"))
            .addStructure(Structure.literal("villages"))
            .build();

    static final Preset THE_VOID = Preset.builder()
            .name("The Void")
            .features(true)
            .biome(Biome.literal("the_void"))
            .addLayer(new Layer(Material.AIR, 1))
            .build();

    static final Preset TUNNELERS_DREAM = Preset.builder()
            .name("Tunnelers' Dream")
            .features(true)
            .biome(Biome.literal("windswept_hills"))
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.STONE, 230))
            .addLayer(new Layer(Material.DIRT, 5))
            .addLayer(new Layer(Material.GRASS_BLOCK, 1))
            .addStructure(Structure.literal("mineshafts"))
            .addStructure(Structure.literal("strongholds"))
            .build();

    static final Preset WATER_WORLD = Preset.builder()
            .name("Water World")
            .biome(Biome.literal("deep_ocean"))
            .addLayer(new Layer(Material.BEDROCK, 1))
            .addLayer(new Layer(Material.DEEPSLATE, 64))
            .addLayer(new Layer(Material.STONE, 5))
            .addLayer(new Layer(Material.DIRT, 5))
            .addLayer(new Layer(Material.GRAVEL, 5))
            .addLayer(new Layer(Material.WATER, 90))
            .addStructure(Structure.literal("ocean_monuments"))
            .addStructure(Structure.literal("ocean_ruins"))
            .addStructure(Structure.literal("shipwrecks"))
            .build();

    static final Set<Preset> PRESETS = Set.of(
            BOTTOMLESS_PIT, CLASSIC_FLAT, DESERT, OVERWORLD,
            REDSTONE_READY, SNOWY_KINGDOM, THE_VOID,
            TUNNELERS_DREAM, WATER_WORLD
    );
}
