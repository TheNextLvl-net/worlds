package net.thenextlvl.worlds.api.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import core.file.format.GsonFile;
import core.io.IO;
import core.paper.adapters.inventory.MaterialAdapter;
import net.thenextlvl.worlds.api.preset.adapter.BiomeTypeAdapter;
import net.thenextlvl.worlds.api.preset.adapter.StructureTypeAdapter;
import org.bukkit.Material;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@NullMarked
public class Preset {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Structure.class, new StructureTypeAdapter())
            .registerTypeAdapter(Material.class, new MaterialAdapter())
            .registerTypeAdapter(Biome.class, new BiomeTypeAdapter())
            .setPrettyPrinting()
            .create();

    private Biome biome = Biome.minecraft("plains");
    private boolean lakes;
    private boolean features;
    private boolean decoration;

    private LinkedHashSet<Layer> layers = new LinkedHashSet<>();
    @SerializedName("structure_overrides")
    private LinkedHashSet<Structure> structures = new LinkedHashSet<>();

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
     * @return a {@code LinkedHashSet} containing the layers of the preset
     */
    public LinkedHashSet<Layer> layers() {
        return layers;
    }

    /**
     * Sets the layers for the preset.
     *
     * @param layers the set of layers to be associated with the preset
     * @return the preset instance for method chaining
     */
    public Preset layers(LinkedHashSet<Layer> layers) {
        this.layers = layers;
        return this;
    }

    /**
     * Retrieves the set of structures associated with the preset.
     *
     * @return a {@code LinkedHashSet} containing the structures of the preset
     */
    public LinkedHashSet<Structure> structures() {
        return structures;
    }

    /**
     * Sets the structures for the preset.
     *
     * @param structures the set of structures to be associated with the preset
     * @return the preset instance for method chaining
     */
    public Preset structures(LinkedHashSet<Structure> structures) {
        this.structures = structures;
        return this;
    }

    /**
     * Add a layer to the preset
     *
     * @param layer the layer to add
     * @return the preset
     */
    public Preset addLayer(Layer layer) {
        layers().add(layer);
        return this;
    }

    /**
     * Add a structure to the preset
     *
     * @param structure the structure to add
     * @return the preset
     */
    public Preset addStructure(Structure structure) {
        structures().add(structure);
        return this;
    }

    /**
     * Save a preset to a file
     *
     * @param file  the file to save the preset to
     * @param force whether to override the file if it already exists
     * @return whether the file could be saved
     */
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
    public JsonObject serialize() {
        return gson.toJsonTree(this).getAsJsonObject();
    }

    @Override
    public String toString() {
        var layers = layers().stream()
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
    public static Preset deserialize(JsonObject object) {
        return gson.fromJson(object, Preset.class);
    }
}
