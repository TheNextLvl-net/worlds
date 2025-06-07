package net.thenextlvl.worlds.api.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import core.file.format.GsonFile;
import core.io.IO;
import core.paper.adapters.inventory.MaterialAdapter;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.preset.adapter.BiomeTypeAdapter;
import net.thenextlvl.worlds.api.preset.adapter.StructureTypeAdapter;
import org.bukkit.Material;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
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
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Structure.class, new StructureTypeAdapter())
            .registerTypeAdapter(Material.class, new MaterialAdapter())
            .registerTypeAdapter(Biome.class, new BiomeTypeAdapter())
            .setPrettyPrinting()
            .create();

    private Biome biome = Biome.literal("plains");
    private boolean lakes;
    private boolean features;
    private boolean decoration;

    private LinkedHashSet<Layer> layers = new LinkedHashSet<>();
    @SerializedName("structure_overrides")
    private LinkedHashSet<Structure> structures = new LinkedHashSet<>();

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
     * @param presetCode the preset code string to parse, in the expected format
     * @return a {@code Preset} object configured with the layers and biome described in the preset code
     * @throws IllegalArgumentException if the preset code contains invalid materials or does not adhere to the required format
     */
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
        return new Preset().layers(layers).biome(Biome.literal(strings[1]));
    }

    /**
     * Retrieves the biome associated with the preset.
     *
     * @return the biome as a {@code Biome} record
     */
    public Biome biome() {
        return biome;
    }

    /**
     * Sets the biome for the current preset.
     *
     * @param biome the biome to be set, encapsulated in a {@code Biome} record
     * @return the current {@code Preset} instance, allowing for method chaining
     */
    public Preset biome(Biome biome) {
        this.biome = biome;
        return this;
    }

    /**
     * Determines whether lakes are enabled for the preset.
     *
     * @return true if lakes are enabled, false otherwise
     */
    public boolean lakes() {
        return lakes;
    }

    /**
     * Sets whether lakes should be included for the preset.
     *
     * @param lakes a boolean indicating whether lakes should be included
     * @return the current Preset instance, allowing for method chaining
     */
    public Preset lakes(boolean lakes) {
        this.lakes = lakes;
        return this;
    }

    /**
     * Determines whether features are enabled for the preset.
     *
     * @return true if features are enabled, false otherwise
     */
    public boolean features() {
        return features;
    }

    /**
     * Sets whether the features should be enabled for the preset.
     *
     * @param features a boolean indicating whether features are enabled
     * @return the current Preset instance, allowing for method chaining
     */
    public Preset features(boolean features) {
        this.features = features;
        return this;
    }

    /**
     * Determines whether decoration is enabled for the preset.
     *
     * @return true if decoration is enabled, false otherwise
     */
    public boolean decoration() {
        return decoration;
    }

    /**
     * Sets whether the decoration should be enabled for the preset.
     *
     * @param decoration a boolean indicating whether decoration is enabled
     * @return the current Preset instance, allowing for method chaining
     */
    public Preset decoration(boolean decoration) {
        this.decoration = decoration;
        return this;
    }

    /**
     * Retrieves the set of layers associated with the preset.
     *
     * @return a {@code Set} containing the layers of the preset
     */
    public @Unmodifiable Set<Layer> layers() {
        return Set.copyOf(layers);
    }

    /**
     * Sets the layers for the preset.
     *
     * @param layers the set of layers to be associated with the preset
     * @return the preset instance for method chaining
     */
    public Preset layers(Set<Layer> layers) {
        this.layers = new LinkedHashSet<>(layers);
        return this;
    }

    /**
     * Retrieves the set of structures associated with the preset.
     *
     * @return a {@code LinkedHashSet} containing the structures of the preset
     */
    public @Unmodifiable Set<Structure> structures() {
        return Set.copyOf(structures);
    }

    /**
     * Sets the structures for the preset.
     *
     * @param structures the set of structures to be associated with the preset
     * @return the preset instance for method chaining
     */
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
    public Preset addStructure(Structure structure) {
        structures.add(structure);
        return this;
    }

    /**
     * Save a preset to a file
     *
     * @param file  the file to save the preset to
     * @param force whether to override the file if it already exists
     * @return whether the file could be saved
     */
    @Deprecated(forRemoval = true)
    public boolean saveToFile(File file, boolean force) {
        if (!force && file.exists()) return false;
        new GsonFile<>(IO.of(file), this, gson).save();
        return true;
    }

    /**
     * Serialize this preset into a JSON object.
     *
     * @return the serialized preset as a JsonObject
     */
    @Deprecated(forRemoval = true)
    public JsonObject serialize() {
        return gson.toJsonTree(this).getAsJsonObject();
    }

    @Override
    public String toString() {
        var layers = this.layers.stream()
                .map(Layer::toString)
                .collect(Collectors.joining(","));
        return layers + ";" + biome();
    }

    /**
     * Deserialize a JSON object into a preset
     *
     * @param object the object to deserialize
     * @return the deserialized preset
     */
    @Deprecated(forRemoval = true)
    public static Preset deserialize(JsonObject object) {
        return gson.fromJson(object, Preset.class);
    }
}
