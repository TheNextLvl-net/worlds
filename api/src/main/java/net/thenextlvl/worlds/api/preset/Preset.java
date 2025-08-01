package net.thenextlvl.worlds.api.preset;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a preset configuration for generating flat world maps.
 * <p>
 * This class allows customization of features such as biomes, layers, structures, decorations, and additional settings.
 * It provides methods for modifying the preset's properties and serializing/deserializing the configuration.
 * <a href="https://minecraft.wiki/w/Superflat#Vanilla_superflat_level_generation_presets">Wiki</a>
 *
 * @see GeneratorType#FLAT
 */
@NullMarked
public class Preset {
    private final @Nullable String name;
    private Biome biome = Biome.literal("plains");
    private boolean lakes;
    private boolean features;
    private boolean decoration;

    private LinkedHashSet<Layer> layers = new LinkedHashSet<>();
    private LinkedHashSet<Structure> structures = new LinkedHashSet<>();

    public Preset() {
        this(null);
    }

    public Preset(@Nullable String name) {
        this.name = name;
    }

    /**
     * Parses a Superflat preset code and generates a corresponding {@code Preset} object.
     * <p>
     * The preset code should follow the format specified in the Superflat preset code
     * <a href="https://minecraft.wiki/w/Superflat#Preset_code_format">documentation</a>.
     * It consists of layer definitions separated by commas and the biome definition separated by a semicolon.
     * Each layer may specify its material and optional height.
     * If the material is invalid or the format is incorrect,
     * an {@code IllegalArgumentException} is thrown.
     *
     * @param presetCode the preset code string to parse in the expected format
     * @return a {@code Preset} object configured with the layers and biome described in the preset code
     * @throws IllegalArgumentException if the preset code contains invalid materials or does not adhere to the required format
     */
    @Contract(pure = true)
    @SuppressWarnings("PatternValidation")
    public static Preset parse(String presetCode) {
        var strings = presetCode.split(";", 2);
        var layers = Arrays.stream(strings[0].split(",")).map(layer -> {
            var parameters = layer.split("\\*", 2);
            var material = parameters.length == 1 ? parameters[0] : parameters[1];
            var height = parameters.length == 1 ? 1 : Integer.parseInt(parameters[0]);
            var matched = Material.matchMaterial(material);
            if (matched != null) return new Layer(matched, height);
            throw new IllegalArgumentException("Invalid material: " + material);
        }).collect(Collectors.toCollection(LinkedHashSet::new));
        return new Preset(null).layers(layers).biome(Biome.literal(strings[1]));
    }

    /**
     * Retrieves the name of the preset.
     *
     * @return the name of the preset as a {@code String}
     */
    @Contract(pure = true)
    public @Nullable String name() {
        return name;
    }

    /**
     * Retrieves the biome associated with the preset.
     *
     * @return the biome as a {@code Biome} record
     */
    @Contract(pure = true)
    public Biome biome() {
        return biome;
    }

    /**
     * Sets the biome for the current preset.
     *
     * @param biome the biome to be set, encapsulated in a {@code Biome} record
     * @return the current {@code Preset} instance, allowing for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Preset biome(Biome biome) {
        this.biome = biome;
        return this;
    }

    /**
     * Determines whether lakes are enabled for the preset.
     *
     * @return true if lakes are enabled, false otherwise
     */
    @Contract(pure = true)
    public boolean lakes() {
        return lakes;
    }

    /**
     * Sets whether lakes should be included for the preset.
     *
     * @param lakes a boolean indicating whether lakes should be included
     * @return the current Preset instance, allowing for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Preset lakes(boolean lakes) {
        this.lakes = lakes;
        return this;
    }

    /**
     * Determines whether features are enabled for the preset.
     *
     * @return true if features are enabled, false otherwise
     */
    @Contract(pure = true)
    public boolean features() {
        return features;
    }

    /**
     * Sets whether the features should be enabled for the preset.
     *
     * @param features a boolean indicating whether features are enabled
     * @return the current Preset instance, allowing for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Preset features(boolean features) {
        this.features = features;
        return this;
    }

    /**
     * Determines whether decoration is enabled for the preset.
     *
     * @return true if decoration is enabled, false otherwise
     */
    @Contract(pure = true)
    public boolean decoration() {
        return decoration;
    }

    /**
     * Sets whether the decoration should be enabled for the preset.
     *
     * @param decoration a boolean indicating whether decoration is enabled
     * @return the current Preset instance, allowing for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Preset decoration(boolean decoration) {
        this.decoration = decoration;
        return this;
    }

    /**
     * Retrieves the set of layers associated with the preset.
     *
     * @return a {@code Set} containing the layers of the preset
     */
    @Contract(pure = true)
    public @Unmodifiable Set<Layer> layers() {
        return Set.copyOf(layers);
    }

    /**
     * Sets the layers for the preset.
     *
     * @param layers the set of layers to be associated with the preset
     * @return the preset instance for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Preset layers(Set<Layer> layers) {
        this.layers = new LinkedHashSet<>(layers);
        return this;
    }

    /**
     * Retrieves the set of structures associated with the preset.
     *
     * @return a {@code LinkedHashSet} containing the structures of the preset
     */
    @Contract(pure = true)
    public @Unmodifiable Set<Structure> structures() {
        return Set.copyOf(structures);
    }

    /**
     * Sets the structures for the preset.
     *
     * @param structures the set of structures to be associated with the preset
     * @return the preset instance for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Preset structures(Set<Structure> structures) {
        this.structures = new LinkedHashSet<>(structures);
        return this;
    }

    /**
     * Add a layer to the preset
     *
     * @param layer the layer to add
     * @return the preset
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Preset addLayer(Layer layer) {
        layers.add(layer);
        return this;
    }

    /**
     * Add a structure to the preset
     *
     * @param structure the structure to add
     * @return the preset
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Preset addStructure(Structure structure) {
        structures.add(structure);
        return this;
    }

    /**
     * Converts the current {@code Preset} object into its corresponding preset code.
     * The generated code represents the layers of the preset and its biome.
     * Layers are serialized into a comma-separated string, followed by a semicolon
     * and the biome string representation.
     * <p>
     * This is a lossy conversion. If you want to save this preset, use {@link #serialize()}.
     *
     * @return a {@code String} containing the serialized layers and biome information of the preset
     */
    @Contract(pure = true)
    public String toPresetCode() {
        var layers = this.layers.stream()
                .map(Layer::toString)
                .collect(Collectors.joining(","));
        return layers + ";" + biome();
    }

    /**
     * Serialize this preset into a JSON object.
     * <a href="https://minecraft.wiki/w/Superflat#Multiplayer">Wiki</a>
     *
     * @return the serialized preset as a JsonObject
     * @see #deserialize(JsonObject)
     */
    @Contract(value = " -> new", pure = true)
    public JsonObject serialize() {
        var root = new JsonObject();
        var layers = new JsonArray();
        var structures = new JsonArray();
        root.addProperty("name", name);
        root.addProperty("biome", biome.key().asString());
        root.addProperty("lakes", lakes);
        root.addProperty("features", features);
        root.addProperty("decoration", decoration);
        this.layers.forEach(layer -> {
            var object = new JsonObject();
            object.addProperty("block", layer.block().key().asString());
            object.addProperty("height", layer.height());
            layers.add(object);
        });
        this.structures.forEach(structure -> structures.add(structure.key().asString()));
        root.add("layers", layers);
        root.add("structure_overrides", structures);
        return root;
    }

    /**
     * Deserialize a JSON object into a preset.
     * <a href="https://minecraft.wiki/w/Superflat#Multiplayer">Wiki</a>
     *
     * @param object the object to deserialize
     * @return the deserialized preset
     * @throws IllegalArgumentException if no layers are provided
     * @see #serialize()
     */
    @SuppressWarnings("PatternValidation")
    @Contract(value = "_ -> new", pure = true)
    public static Preset deserialize(JsonObject object) throws IllegalArgumentException {
        Preconditions.checkArgument(object.has("layers"), "Missing layers");
        var preset = new Preset(object.has("name") ? object.get("name").getAsString() : null);
        if (object.has("biome")) preset.biome(Biome.literal(object.get("biome").getAsString()));
        if (object.has("lakes")) preset.lakes(object.get("lakes").getAsBoolean());
        if (object.has("features")) preset.features(object.get("features").getAsBoolean());
        if (object.has("decoration")) preset.decoration(object.get("decoration").getAsBoolean());
        object.getAsJsonArray("layers").forEach(layer -> {
            var layerObject = layer.getAsJsonObject();
            var material = Material.matchMaterial(layerObject.get("block").getAsString());
            var height = layerObject.get("height").getAsInt();
            if (material != null) preset.addLayer(new Layer(material, height));
        });
        if (object.has("structure_overrides")) object.getAsJsonArray("structure_overrides")
                .forEach(structure -> preset.addStructure(Structure.literal(structure.getAsString())));
        return preset;
    }

    @Override
    public String toString() {
        return "Preset{" +
               "name='" + name + '\'' +
               ", biome=" + biome +
               ", lakes=" + lakes +
               ", features=" + features +
               ", decoration=" + decoration +
               ", layers=" + layers +
               ", structures=" + structures +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Preset preset = (Preset) o;
        return lakes == preset.lakes && features == preset.features && decoration == preset.decoration && Objects.equals(name, preset.name) && Objects.equals(biome, preset.biome) && Objects.equals(layers, preset.layers) && Objects.equals(structures, preset.structures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, biome, lakes, features, decoration, layers, structures);
    }
}
