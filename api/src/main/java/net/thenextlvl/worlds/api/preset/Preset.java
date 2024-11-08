package net.thenextlvl.worlds.api.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import core.file.format.GsonFile;
import core.io.IO;
import core.paper.adapters.inventory.MaterialAdapter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.thenextlvl.worlds.api.preset.adapter.BiomeTypeAdapter;
import net.thenextlvl.worlds.api.preset.adapter.StructureTypeAdapter;
import org.bukkit.Material;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class Preset {
    private Biome biome = Biome.minecraft("plains");
    private boolean lakes;
    private boolean features;
    private boolean decoration;

    private LinkedHashSet<Layer> layers = new LinkedHashSet<>();
    @SerializedName("structure_overrides")
    private LinkedHashSet<Structure> structures = new LinkedHashSet<>();

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
     * Serialize this preset into a json object.
     *
     * @return the serialized preset as a JsonObject
     */
    public JsonObject serialize() {
        return gson.toJsonTree(this).getAsJsonObject();
    }

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Structure.class, new StructureTypeAdapter())
            .registerTypeAdapter(Material.class, MaterialAdapter.instance())
            .registerTypeAdapter(Biome.class, new BiomeTypeAdapter())
            .setPrettyPrinting()
            .create();

    /**
     * Deserialize a json object into a preset
     *
     * @param object the object to deserialize
     * @return the deserialized preset
     */
    public static Preset deserialize(JsonObject object) {
        return gson.fromJson(object, Preset.class);
    }

    @Override
    public String toString() {
        var layers = layers().stream()
                .map(Layer::toString)
                .collect(Collectors.joining(","));
        return layers + ";" + biome();
    }
}
